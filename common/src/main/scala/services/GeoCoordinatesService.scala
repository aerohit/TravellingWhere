package services

import scala.util.Try

case class GeoCoordinate(country: String, city: String, latitude: String, longitude: String) {
  def serializeToString(): String =
    s"$country|$city|$latitude|$longitude"
}

object GeoCoordinate {
  def apply(line: String): GeoCoordinate = {
    val cols = line.split('|').toList
    GeoCoordinate(cols(1), cols(2), cols(4), cols(5))
  }

  def parseFromString(s: String): Option[GeoCoordinate] = {
    Try {
      val fields = s.split('|')
      GeoCoordinate(fields(0), fields(1), fields(2), fields(3))
    }.toOption
  }
}

object GeoCoordinatesService extends App {

  def enrich(url: String) = {
    // TODO: figure out a way to not have to make this a local variable
    val lines =
      """
        |/netherlands/amsterdam/|Netherlands|Amsterdam|NL|52.37403|4.88969
        |/turkey/ankara/|Turkey|Ankara|TR|39.91987|32.85427
        |/greece/athens/|Greece|Athens|GR|37.97945|23.71622
        |/germany/berlin/|Germany|Berlin|DE|52.52437|13.41053
        |/switzerland/bern/|Switzerland|Bern|CH|46.94809|7.44744
        |/slovakia/bratislava/|Slovakia|Bratislava|SK|48.14816|17.10674
        |/belgium/brussels/|Belgium|Brussels|BE|50.85045|4.34878
        |/hungary/budapest/|Hungary|Budapest|HU|47.49801|19.03991
        |/romania/bucharest/|Romania|Bucharest|RO|44.43225|26.10626
        |/denmark/copenhagen/|Denmark|Copenhagen|DK|55.67594|12.56553
        |/ireland/dublin/|Ireland|Dublin|IE|53.33306|-6.24889
        |/finland/helsinki/|Finland|Helsinki|FI|60.16952|24.93545
        |/ukraine/kiev/|Ukraine|Kiev|UA|50.45466|30.5238
        |/portugal/lisbon/|Portugal|Lisbon|PT|38.71667|-9.13333
        |/slovenia/ljubljana/|Slovenia|Ljubljana|SI|46.05108|14.50513
        |/united-kingdom/london/|United Kingdom|London|GB|51.50853|-0.12574
        |/luxembourg/luxembourg/|Luxembourg|Luxembourg|LU|49.61167|6.13
        |/spain/madrid/|Spain|Madrid|ES|40.4165|-3.70256
        |/belarus/minsk/|Belarus|Minsk|BY|53.9|27.56667
        |/monaco/monaco/|Monaco|Monaco|MC|43.73333|7.41667
        |/russia/moscow/|Russia|Moscow|RU|55.75222|37.61556
        |/norway/oslo/|Norway|Oslo|NO|59.91273|10.74609
        |/france/paris/|France|Paris|FR|48.85341|2.3488
        |/czech-republic/prague/|Czech Republic|Prague|CZ|50.08804|14.42076
        |/latvia/riga/|Latvia|Riga|LV|56.946|24.10589
        |/italy/rome/|Italy|Rome|IT|41.89193|12.51133
        |/sweden/stockholm/|Sweden|Stockholm|SE|59.33258|18.0649
        |/estonia/tallinn/|Estonia|Tallinn|EE|59.43696|24.75353
        |/lithuania/vilnius/|Lithuania|Vilnius|LT|54.68916|25.2798
        |/poland/warsaw/|Poland|Warsaw|PL|52.22977|21.01178
        |/austria/vienna/|Austria|Vienna|AT|48.20849|16.37208
        |/croatia/zagreb/|Croatia|Zagreb|HR|45.81444|15.97798
      """.stripMargin.split("\n")

    val key = url.split("places")(1)
    lines
      .find(_.startsWith(key))
      .map(GeoCoordinate.apply)
  }

  println(enrich("/places/netherlands/amsterdam/"))
}
