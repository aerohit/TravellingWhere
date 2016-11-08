package formatters

import play.api.libs.json.Json
import services.{GeoCodeWithCount, GeoCoordinate}

object JsonFormatters {
  implicit val geoCoordinateFormat = Json.format[GeoCoordinate]
  implicit val geoCoordinateWithCountFormat = Json.format[GeoCodeWithCount]
}
