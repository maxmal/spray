/*
 * Copyright (C) 2011 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.spray
package typeconversion

import http._
import MediaTypes._
import HttpCharsets._
import xml.NodeSeq
import java.nio.CharBuffer

trait DefaultMarshallers extends MultipartMarshallers {

  implicit val StringMarshaller = new MarshallerBase[String] {
    val canMarshalTo = ContentType(`text/plain`) :: Nil

    def marshal(value: String, contentType: ContentType) = HttpContent(contentType, value)
  }

  implicit val CharArrayMarshaller = new MarshallerBase[Array[Char]] {
    val canMarshalTo = ContentType(`text/plain`) :: Nil

    def marshal(value: Array[Char], contentType: ContentType) = {
      val nioCharset = contentType.charset.getOrElse(`ISO-8859-1`).nioCharset
      val charBuffer = CharBuffer.wrap(value)
      val byteBuffer = nioCharset.encode(charBuffer)
      HttpContent(contentType, byteBuffer.array)
    }
  }
  
  implicit val NodeSeqMarshaller = new MarshallerBase[NodeSeq] {
    val canMarshalTo = ContentType(`text/xml`) ::
                       ContentType(`text/html`) ::
                       ContentType(`application/xhtml+xml`) :: Nil

    def marshal(value: NodeSeq, contentType: ContentType) = StringMarshaller.marshal(value.toString, contentType)
  }

  implicit val FormDataMarshaller = new MarshallerBase[FormData] {
    val canMarshalTo = ContentType(`application/x-www-form-urlencoded`) :: Nil

    def marshal(formContent: FormData, contentType: ContentType) = {
      import java.net.URLEncoder.encode
      val charset = contentType.charset.getOrElse(`ISO-8859-1`).aliases.head
      val keyValuePairs = formContent.fields.map {
        case (key, value) => encode(key, charset) + '=' + encode(value, charset)
      }
      StringMarshaller.marshal(keyValuePairs.mkString("&"), contentType)
    }
  }
}

object DefaultMarshallers extends DefaultMarshallers