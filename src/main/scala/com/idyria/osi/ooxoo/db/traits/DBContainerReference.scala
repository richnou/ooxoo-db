package com.idyria.osi.ooxoo.db.traits

import com.idyria.osi.ooxoo.db.store.DocumentContainer

trait DBContainerReference {
  
   var parentContainer : Option[DocumentContainer] = None
}