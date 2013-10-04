package com.idyria.osi.ooxoo.db.store

import com.idyria.osi.ooxoo.core.buffers.structural.ElementBuffer
import com.idyria.osi.ooxoo.core.buffers.structural.io.sax.StAXIOBuffer
import com.idyria.osi.ooxoo.db.Document
import java.lang.ref.WeakReference
import com.idyria.osi.ooxoo.core.buffers.structural.Buffer
import com.idyria.osi.ooxoo.db.store.fs.FSDocument
import java.io.File
import com.idyria.osi.tea.listeners.ListeningSupport

/**
 * A Document Container really contains documents and fetches/stores them
 */
trait DocumentContainer extends ListeningSupport {

  

  // Infos
  //----------------
  
  /**
   * The id mandatory for a container
   * Each container must be uniquely identified by an id
   *
   */
  var id: String
  
  /**
   * Lists all the documents available
   */
  def documents : Iterable[Document]
  
  // Pure Document Interface
  //-------------------------

  /**
   * Caches parsed documents 
   * The Reference to parsed data is Weak to allow Garbage collection if needed
   */
  var parsedDocumentCache = Map[String,WeakReference[ElementBuffer]]()
  
  
  /**
   * Retrieve a Document interface from document
   */
  def document(path: String): Option[Document]

  /**
   * Retrieve a document from container and parse it using OOXOO core
   * 
   * FIXME: Add some more type testing with cache to avoid two document() calls with different type causing classCastException
   * @return None or and Option containing the provided topElement 
   */
  def document[T <: ElementBuffer](path: String, topElement: T): Option[T] = {
		
    //-- Look into cache
    this.parsedDocumentCache.get(path) match {
      
      //-- In Map and reference is not weak, return
      case Some(reference) if(reference.get()!=null)=> Option(reference.get().asInstanceOf[T])  
            
      //-- In map, but reference is weak
      //-- Or None
      //-- Try to parse
      case None => 
        
        //-- Get Document
	    this.document(path) match {
	      case None => None
	      case Some(document) => 
	        
	        //-- Parse
	        var io = new StAXIOBuffer(document.toInputStream)
	        topElement.appendBuffer(io)
	        io.streamIn
	        
	        //-- Return
	        Option(topElement)
	    }
        
    }
    
    
  }
  
  /**
   * Write a Buffer type to a document
   * 
   * Listening point:
   * 
   * document.writen((path,topElement))
   */
  def writeDocument[T <: Buffer](path: String, topElement: T): Unit
    

}