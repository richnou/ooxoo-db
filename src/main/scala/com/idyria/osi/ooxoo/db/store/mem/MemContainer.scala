package com.idyria.osi.ooxoo.db.store.mem

import com.idyria.osi.ooxoo.db.store.DocumentContainer
import com.idyria.osi.ooxoo.db.Document
import com.idyria.osi.ooxoo.core.buffers.structural.ElementBuffer
import scala.reflect.ClassTag

class MemContainer(var id : String)  extends DocumentContainer {
  
  var documentsMap = Map[String,MemDocument]()
  
 /** As seen from class MemContainer, the missing signatures are as follows.
 *  For convenience, these are usable as stub implementations.
 */
  def documents: Iterable[com.idyria.osi.ooxoo.db.Document] = {
    documentsMap.values
  }
  
  def getDocument(path: String): Option[com.idyria.osi.ooxoo.db.Document] = {
    documentsMap.get(path)
  }
  def writeDocument[T <: ElementBuffer : ClassTag](path: String,topElement: T): Document  = {
    
    documentsMap.get(path) match {
      case Some(memdocument) => 
        memdocument.elt = topElement
      memdocument
      case None =>  
        var doc =  new MemDocument(path,topElement)
        documentsMap = documentsMap.updated(path,doc)
        doc
    }
   
  }
}

class MemDocument(var id : String ,var elt:ElementBuffer) extends Document {
  
  def exists: Boolean = {
    true
  }
//  def id_=(x$1: String): Unit = ???
  def toInputStream: java.io.InputStream = {
    null
  }
  def toOutputStream: java.io.OutputStream = {
    null
  }
  
  
}