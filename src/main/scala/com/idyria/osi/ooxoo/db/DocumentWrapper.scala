package com.idyria.osi.ooxoo.db

import com.idyria.osi.ooxoo.core.buffers.structural.ElementBuffer
import scala.reflect.ClassTag

class DocumentWrapper[T <: ElementBuffer: ClassTag](val node : T,val doc : Document) {
  
  
  def sync = {
    
    doc.container.writeDocument(doc.id, node)
    
  }
  
  
}