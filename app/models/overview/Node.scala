package models.overview

import models.commons.NodeRoles
import play.api.libs.json._

object Node {

  def apply(id: String, info: JsValue, stats: JsValue, masterNodeId: String) = {
    val nodeRoles = NodeRoles(stats)

    // AWS nodes return no host/ip info
    val host = (stats \ "host").asOpt[JsString].getOrElse(JsNull)
    val ip = (stats \ "ip").asOpt[JsString].getOrElse(JsNull)
    val jvmVersion = (info \ "jvm" \ "version").asOpt[JsString].getOrElse(JsNull)

    val esVersion = (info \ "version").as[String]
    val availableProcessors = (info \ "os" \ "available_processors").as[Int]

    Json.obj(
      "id" -> JsString(id),
      "current_master" -> JsBoolean(id.equals(masterNodeId)),
      "name" -> (stats \ "name").as[JsString],
      "host" -> host,
      "ip" -> ip,
      "es_version" -> esVersion,
      "jvm_version" -> jvmVersion,
      "load_average" -> loadAverage(stats, availableProcessors, esVersion),
      "available_processors" -> availableProcessors,
      "cpu_percent" -> cpuPercent(stats, esVersion),
      "master" -> JsBoolean(nodeRoles.master),
      "data" -> JsBoolean(nodeRoles.data),
      "coordinating" -> JsBoolean(nodeRoles.coordinating),
      "ingest" -> JsBoolean(nodeRoles.ingest),
      "heap" -> Json.obj(
        "used" -> (stats \ "jvm" \ "mem" \ "heap_used_in_bytes").as[JsNumber],
        "committed" -> (stats \ "jvm" \ "mem" \ "heap_committed_in_bytes").as[JsNumber],
        "used_percent" -> (stats \ "jvm" \ "mem" \ "heap_used_percent").as[JsNumber],
        "max" -> (stats \ "jvm" \ "mem" \ "heap_max_in_bytes").as[JsNumber]
      ),
      "disk" -> disk(stats)
    )
  }

  def disk(stats: JsValue): JsObject = {
    val totalInBytes = (stats \ "fs" \ "total" \ "total_in_bytes").asOpt[Long].getOrElse(0l)
    val freeInBytes = (stats \ "fs" \ "total" \ "free_in_bytes").asOpt[Long].getOrElse(0l)
    val usedPercent = 100 - (100 * (freeInBytes.toFloat / totalInBytes.toFloat)).toInt
    Json.obj(
      "total" -> JsNumber(totalInBytes),
      "free" -> JsNumber(freeInBytes),
      "used_percent" -> JsNumber(usedPercent)
    )
  }

  def loadAverage(nodeStats: JsValue, availableProcessors: Int, esVersion: String): JsNumber = {
    val load = parseLoadAverage(nodeStats, availableProcessors, esVersion).getOrElse(0.0)
    JsNumber(BigDecimal(load))
  }

  private def parseLoadAverage(stats: JsValue, availableProcessors: Int, esVersion: String): Option[Double] = {
    if (esVersion.startsWith("1.")) {
      val cpuLoadAverage = (stats \ "os" \ "load_average").asOpt[JsArray].getOrElse(JsArray.empty)
      return cpuLoadAverage.head.asOpt[Double]
    }
    if (esVersion.startsWith("2.")) {
      return (stats \ "os" \ "load_average").asOpt[Double]
    }
    (stats \ "os" \ "cpu" \ "load_average" \ "1m").asOpt[Double]
  }

  def cpuPercent(nodeStats: JsValue, esVersion: String): JsNumber = {
    val cpu = parseCpuPercent(nodeStats, esVersion).getOrElse(0)
    JsNumber(BigDecimal(cpu))
  }

  private def parseCpuPercent(nodeStats: JsValue, esVersion: String): Option[Int] = {
    if (esVersion.startsWith("1.")) {
      return (nodeStats \ "os" \ "cpu" \ "user").asOpt[Int]
    }
    if (esVersion.startsWith("2.")) {
      return (nodeStats \ "os" \ "cpu_percent").asOpt[Int]
    }
    (nodeStats \ "os" \ "cpu" \ "percent").asOpt[Int]
  }
}
