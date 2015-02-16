package com.idyria.osi.ooxoo.db.store

import com.idyria.osi.ooxoo.core.buffers.structural.ElementBuffer
import com.idyria.osi.ooxoo.core.buffers.structural.io.sax.StAXIOBuffer
import com.idyria.osi.ooxoo.db.Document
import java.lang.ref.WeakReference
import com.idyria.osi.ooxoo.core.buffers.structural.Buffer
import com.idyria.osi.ooxoo.db.store.fs.FSDocument
import java.io.File
import com.idyria.osi.tea.listeners.ListeningSupport
import scala.reflect._

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
  def documents: Iterable[Document]

  /**
   * Returns the list of all Documents that are mappable to the provided parameter type
   */
  def documentsAs[T <: ElementBuffer: ClassTag]: Iterable[T] = {

    documents.map { d => document[T](d.id) }.filterNot { _ == None }.map(_.get)

  }

  // Pure Document Interface
  //-------------------------

  /**
   * Caches parsed documents
   * The Reference to parsed data is Weak to allow Garbage collection if needed
   */
  var parsedDocumentCache = Map[String, WeakReference[ElementBuffer]]()

  /**
   * Retrieve a Document interface from document
   */
  def getDocument(path: String): Option[Document]

  def getCached[T <: ElementBuffer: ClassTag](id: String): Option[T] = {

    //println(s"**DB** Looking for Cached document: ${this.id}/$id")

    //-- Look into cache
    this.parsedDocumentCache.get(id) match {

      //-- In Map, reference is not weak and types match
      //case Some(reference) if (reference.get() != null && Thread.currentThread.getContextClassLoader().loadClass(s"${classTag[T]}").isAssignableFrom(reference.get().getClass)) =>
      case Some(reference) if (reference.get() != null) =>

        Option(reference.get().asInstanceOf[T])

      //-- In map and reference is weak, return none
      //-- NOthing, return none
      case Some(reference) if (reference.get != null) =>
       // println(s"**DB** Rebuilding ${this.id}/$id because class tag does not match : ${reference.get().getClass.getClassLoader.hashCode()} <-> ${classTag[T]}")
        None
      case _ =>
      //  println(s"**DB** Rebuilding ${this.id}/$id because reference is gone")
        None

    }

  }

  /**
   * Retrieve a document from container and parse it using OOXOO core
   *
   * FIXME: Add some more type testing with cache to avoid two document() calls with different type causing classCastException
   * @return None or and Option containing the provided topElement
   */
  def document[T <: ElementBuffer: ClassTag](path: String, topElement: T): Option[T] = {

    //-- Look into cache
    this.getCached[T](path) match {

      //-- In Map and reference is not weak, return
      case Some(res) => Option(res)

      //-- In map, but reference is weak
      //-- Or None
      //-- Try to parse
      case None =>

        //-- Get Document
        this.getDocument(path) match {
          case None => None
          case Some(document) =>

            //-- Parse
            var io = new StAXIOBuffer(document.toInputStream)
            topElement.appendBuffer(io)
            io.streamIn

            //-- Cache
            this.parsedDocumentCache = this.parsedDocumentCache + (document.id -> new WeakReference(topElement))

            //-- Return
            Option(topElement)
        }

    }

  }

  /**
   * Retrieve a document from container and parse it using OOXOO core
   *
   * FIXME: Add some more type testing with cache to avoid two document() calls with different type causing classCastException
   * @return None or and Option containing the provided topElement
   */
  def document[T <: ElementBuffer: ClassTag](path: String): Option[T] = {

    //-- Look into cache
    this.getCached[T](path) match {

      //-- In Map and reference is not weak, return
      case Some(res) => Option(res)

      //-- In map, but reference is weak
      //-- Or None
      //-- Try to parse
      case None =>

        //-- Get Document
        this.getDocument(path) match {
          case None => None
          case Some(document) =>

            // Create top Document
            var top = Thread.currentThread.getContextClassLoader().loadClass(s"${classTag[T]}").newInstance.asInstanceOf[T]
            //println("Reflection instancitation of "+classTag[T])
            //classTag[T].

            //-- Parse
            var io = new StAXIOBuffer(document.toInputStream)
            top.appendBuffer(io)
            io.streamIn

            //-- Cache
            this.parsedDocumentCache = this.parsedDocumentCache + (document.id -> new WeakReference(top))

            //-- Return
            Option(top)

        }

    }

  }

  /**
   * Gets all the Documents from current container, that match the given type
   */
  def getAllDocuments[T <: ElementBuffer: ClassTag]: List[T] = {

    // Prepare results
    //---------------
    var res = List[T]()

    // Go through all documents an try to parse
    this.documents.foreach {
      doc =>

        //-- Try to get cached
        this.getCached[T](doc.id) match {
          case Some(parsed) => res = res :+ parsed

          //-- Nothing cached, try to parse
          case None =>

            try {

              //-- Prepare a top element
              var top = Thread.currentThread.getContextClassLoader().loadClass(s"${classTag[T]}").newInstance.asInstanceOf[T]
              this.document[T](doc.id, top)

              //-- OK
              res = res :+ top
              //top = Thread.currentThread.getContextClassLoader().loadClass(s"${classTag[T]}").newInstance.asInstanceOf[T]

            } catch {
              // Error , don't retain this document
              case e: Throwable =>
            }

        }

    }
    res
  }

  /**
   * Write a Buffer type to a document
   *
   * Listening point:
   *
   * document.writen((path,topElement))
   */
  def writeDocument[T <: ElementBuffer: ClassTag](path: String, topElement: T): Unit

}