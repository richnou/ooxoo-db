package com.idyria.osi.ooxoo.db.store

import com.idyria.osi.ooxoo.db.Document
import com.idyria.osi.ooxoo.core.buffers.structural.ElementBuffer
import scala.reflect.ClassTag

/**
 * Base Trait for Document Store
 *
 * A Document Store is a main interface to fetch documents
 */
trait DocumentStore {

  // Container Interface
  //--------------------------
  
  /**
   * Returns a container for the provided id
   */
  def container(id: String): DocumentContainer

  /**
   * Returns a list of all known containers
   */
  def containers() : Iterable[DocumentContainer]
  
  // Document Interface
  //-----------------------

  /**
   * path format:  "containerid"/"documentid"
   */
  def document(path: String): Option[Document] = {

    path.split("/") match {

      case splitted if (splitted.length != 2) => throw new RuntimeException(s"""DocumentStore document path $path not conform to containerid/documentid format """)

      case splitted                           => this.container(splitted(0)).getDocument(splitted(1))

    }

  }

  def document[T <: ElementBuffer : ClassTag](path: String, topElement: T): Option[T] = {

    path.split("/") match {

      case splitted if (splitted.length != 2) => throw new RuntimeException(s"""DocumentStore document path $path not conform to containerid/documentid format """)

      case splitted                           => this.container(splitted(0)).document[T](splitted(1), topElement)

    }

  }

  // XPath Interface
  //---------------------

}

