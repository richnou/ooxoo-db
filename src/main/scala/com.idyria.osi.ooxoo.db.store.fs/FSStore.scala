package com.idyria.osi.ooxoo.db.store.fs

import com.idyria.osi.ooxoo.db.store.DocumentStore
import java.io.File
import com.idyria.osi.ooxoo.db.store.DocumentContainer
import com.idyria.osi.ooxoo.db.Document
import java.io.FileInputStream
import com.idyria.osi.ooxoo.core.buffers.structural.Buffer
import com.idyria.osi.ooxoo.core.buffers.structural.io.sax.StAXIOBuffer
import java.io.FileOutputStream
import scala.reflect.ClassTag
import com.idyria.osi.ooxoo.core.buffers.structural.ElementBuffer

/**
 * FSSStore is a simple filesystem store implementation
 */
class FSStore(

    /**
     * The Base Folder where containers are stored
     */
    var baseFolder: File) extends DocumentStore {

  // Utils
  //------------
  def cleanId(id: String): String = id.trim.replaceAll(s"""(/|${File.separator})""", ".")

  // Containers
  //-----------------

  /**
   * Map holding the created container up to now
   */
  var containerCache = Map[String, FSContainer]()

  /**
   * id format: any string but no "/", they will be converted to .
   */
  def container(id: String): FSContainer = {

    // Clean id
    var cleanedId = cleanId(id)

    // Look in cache, if not create
    containerCache.get(cleanedId) match {
      case Some(container) => container

      // Create and add
      case None =>

        //-- Create Folder and check
        var containerFolder = new File(baseFolder, cleanedId)
        if (containerFolder.exists() && !containerFolder.isDirectory()) {
          throw new RuntimeException(s"Could not create FSContainer $cleanedId because file already exists and is not a folder: ${containerFolder.getAbsolutePath()}")
        }
        containerFolder.mkdirs()

        //-- Create container
        var container = new FSContainer(containerFolder)
        this.containerCache = this.containerCache + (cleanedId -> container)
        container

    }

  }

  /**
   * List all the folders and create a container for erach
   */
  def containers(): Iterable[FSContainer] = {

    this.baseFolder.listFiles() match {
      case null  => Nil
      case files => files.filter(_.isDirectory()).map(f => container(f.getName()))
    }

  }

  // XPath interface
  //----------------

}

class FSContainer(

    /**
     * Base folder of container where documents are stored
     */
    var baseFolder: File) extends DocumentContainer {

  // Constructor
  //---------------

  //-- Container Id is the folder name
  var id = baseFolder.getName()

  // Infos
  //-----------------

  /**
   * List folder content, and create a document for all the files
   */
  def documents: Iterable[Document] = {

    this.baseFolder.listFiles() match {
      case null  => Nil
      case files => files.filter(_.isFile()).map(f => this.getDocument(f.getName).get)
    }

  }

  // Document
  //---------------------------

  /**
   * A document is an XML File in the base folder
   */
  def getDocument(path: String): Option[Document] = {

    //-- Append xml to path
    var filePath = path match {
      case path if (path.endsWith("xml")) => path
      case _                              => s"$path.xml"
    }

    //-- Create File
    var documentFile = new File(baseFolder, filePath)

    //-- Exists ?
    documentFile.exists() match {
      case true  => Option(new FSDocument(documentFile))
      case flase => None
    }

  }

  /**
   *
   */
  def writeDocument[T <: ElementBuffer: ClassTag](path: String, topElement: T): Unit = {

    //-- Append xml to path
    var filePath = path match {
      case path if (path.endsWith("xml")) => path
      case _                              => s"$path.xml"
    }

    //-- Get Document 
    var document = this.getDocument(path) match {
      case Some(doc) => doc

      //-- Create   
      case None =>

        var doc = new FSDocument(new File(baseFolder, filePath))
        doc
    }

    //-- Add IO Buffer
    var io = new StAXIOBuffer()
    io.output = document.toOutputStream
    topElement.appendBuffer(io)
    topElement.streamOut()

    //-- Listeners call
    this.@->("document.writen", (path, topElement))
    this.@->("document.writen")

  }

}

class FSDocument(

    /**
     * The File holding document data
     */
    var file: File) extends Document {

  var id = file.getName

  def toInputStream = new FileInputStream(file)

  def toOutputStream = new FileOutputStream(file)

}
