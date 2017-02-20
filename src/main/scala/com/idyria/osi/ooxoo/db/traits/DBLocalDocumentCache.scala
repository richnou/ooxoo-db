package com.idyria.osi.ooxoo.db.traits

import com.idyria.osi.ooxoo.core.buffers.structural.ElementBuffer
import java.io.File
import scala.reflect.ClassTag
import java.lang.ref.WeakReference
import com.idyria.osi.ooxoo.core.buffers.structural.io.sax.STAXSyncTrait
import com.idyria.osi.ooxoo.core.buffers.structural.io.sax.StAXIOBuffer

trait DBLocalDocumentCache {
  
  var documentsCache = Map[String,WeakReference[ElementBuffer]]()
  
  def getCachedDocument[T <: ElementBuffer](name:String,file:File)(implicit tag : ClassTag[T]) : Option[T] = {
    
    documentsCache.get(name) match {
      case Some(doc) if (doc.get()!=null && tag.runtimeClass.isAssignableFrom(doc.getClass)) => Some(doc.asInstanceOf[T]) 
      case others if(file.exists()==false) => None
      case others => 
        
        //-- Instanciate
        var elt = tag.runtimeClass.newInstance().asInstanceOf[ElementBuffer]
        
        //-- Streamin
        elt match {
          case stx : STAXSyncTrait => 
            stx.fromFile(file)
          case other => 
            
            var b =  StAXIOBuffer(file.toURI().toURL())
            elt.appendBuffer(b)
            b.streamIn
            b
            
        }
        
        //-- Save
        this.documentsCache = this.documentsCache + (name -> new WeakReference(elt))
        
        Some(elt.asInstanceOf[T])
        
    }
    
  }
  
}