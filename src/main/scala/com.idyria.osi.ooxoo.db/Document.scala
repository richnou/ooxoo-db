package com.idyria.osi.ooxoo.db

import java.io.InputStream
import java.io.OutputStream

/**
 * Common trait to represent a document extracted from Document store
 * 
 * The common DocumentStore uses this interface to get bytes and parse
 */
trait Document {
  
  /**
   * A Unique id for this document inside a container
   */
  var id : String
  
  /**
   * Open A Stream to read from this document
   */
  def toInputStream : InputStream
  
  /**
   * Open a stream to write to this document
   */
  def toOutputStream : OutputStream
  
}