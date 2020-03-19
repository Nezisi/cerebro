package models.nodes

import models.commons.NodeRoles
import play.api.libs.json._

import scala.math.BigDecimal.RoundingMode

object Node {

  def apply(id: String, currentMaster: Boolean, info: JsValue, stats: JsValue): JsValue = {
    val jvmVersion = (info \ "jvm" \ "version").asOpt[JsString].getOrElse(JsNull)
    val esVersion = (info \ "version").as[String]
    val availableProcessors = (info \ "os" \ "available_processors").asOpt[Int].getOrElse(1)

    Json.obj(
      "id" -> JsString(id),
      "current_master" -> JsBoolean(currentMaster),
      "name" -> (stats \ "name").as[JsValue],
      "host" -> (stats \ "host").asOpt[JsValue],
      "heap" -> heap(stats),
      "disk" -> disk(stats),
      "cpu" -> cpu(stats, availableProcessors, esVersion),
      "uptime" -> (stats \ "jvm" \ "uptime_in_millis").as[JsValue],
      "jvm" -> jvmVersion,
      "version" -> (info \ "version").as[JsValue]
    ) ++ roles(info)
  }

  private def roles(info: JsValue): JsObject = {
    val roles = NodeRoles(info)
    Json.obj(
      "master" -> JsBoolean(roles.master),
      "coordinating" -> JsBoolean(roles.coordinating),
      "ingest" -> JsBoolean(roles.ingest),
      "data" -> JsBoolean(roles.data)
    )
  }

  private def cpu(stats: JsValue, availableProcessors: Int, esVersion: String): JsValue = {
    Json.obj(
      "process" -> parseCpuPercent(stats, availableProcessors, esVersion),
      "os" -> parseCpuOs(stats, esVersion),
      "load" -> parseLoadAverage(stats, esVersion),
      "available_processors" -> availableProcessors
    )
  }

  private def parseCpuPercent(stats: JsValue, availableProcessors: Int, esVersion: String): Option[JsNumber] = {
    if (esVersion.startsWith("1.")) {
      val cpuPercent = (stats \ "process" \ "cpu" \ "percent").asOpt[Double].getOrElse(0.0)
      val cpuPercent100 = BigDecimal(cpuPercent / (availableProcessors -1 )) .setScale(2, RoundingMode.HALF_UP)
      return Option[JsNumber](JsNumber(cpuPercent100))
    }
    (stats \ "process" \ "cpu" \ "percent").asOpt[JsNumber]
  }

  private def parseLoadAverage(stats: JsValue, esVersion: String): Option[JsValue] = {
    if (esVersion.startsWith("1.")) {
      return (stats \ "os" \ "load_average").asOpt[JsArray].getOrElse(JsArray.empty).head.toOption
    }
    if (esVersion.startsWith("2.")) {
      return (stats \ "os" \ "load_average").asOpt[JsValue]
    }
    (stats \ "os" \ "cpu" \ "load_average" \ "1m").asOpt[JsValue]
  }

  private def parseCpuOs(stats: JsValue, esVersion: String): Option[JsValue] = {
    if (esVersion.startsWith("1.")) {
      return (stats \ "os" \ "cpu" \ "usage").asOpt[JsValue]
    }
    if (esVersion.startsWith("2.")) {
      return (stats \ "os" \ "cpu_percent").asOpt[JsValue]
    }
    (stats \ "os" \ "cpu" \ "percent").asOpt[JsValue]
  }

  private def disk(stats: JsValue): JsValue = {
    val total = (stats \ "fs" \ "total" \ "total_in_bytes").asOpt[Long]
    val available = (stats \ "fs" \ "total" \ "available_in_bytes").asOpt[Long]
    (total, available) match {
      case (Some(t), Some(a)) =>
        val percent = Math.round((1 - (a.toFloat / t.toFloat)) * 100)
        Json.obj(
          "total" -> JsNumber(t),
          "available" -> JsNumber(a),
          "percent" -> JsNumber(percent)
        )
      case _ => JsNull
    }
  }

  private def heap(stats: JsValue): JsValue =
    Json.obj(
      "max" -> (stats \ "jvm" \ "mem" \ "heap_max").as[JsValue],
      "used" -> (stats \ "jvm" \ "mem" \ "heap_used").as[JsValue],
      "percent" -> (stats \ "jvm" \ "mem" \ "heap_used_percent").as[JsValue]
    )

}
