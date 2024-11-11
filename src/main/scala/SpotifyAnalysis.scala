import spray.json._
import spray.json.DefaultJsonProtocol._
import sttp.client4.httpurlconnection.HttpURLConnectionBackend
import sttp.client4.quick._

/*
Author: Vishnu Rao-Sharma
 */


object SpotifyAnalysis extends App {
  // Spotify stuff
  val accessToken = ""
  val playlistId = "5Rrf7mqN8uus2AaQQQNdc1"

  // Tuples for JSON parsing
  case class Artist(id: String, name: String)
  case class Track(name: String, duration_ms: Long, artists: List[Artist])
  case class ArtistDetails(name: String, followers: Followers)
  case class Followers(total: Int)
  case class PlaylistTracks(items: List[PlaylistItem])
  case class PlaylistItem(track: Track)

  // Format the JSON
  implicit val followersFormat: RootJsonFormat[Followers] = jsonFormat1(Followers)
  implicit val artistDetailsFormat: RootJsonFormat[ArtistDetails] = jsonFormat2(ArtistDetails)
  implicit val artistFormat: RootJsonFormat[Artist] = jsonFormat2(Artist)
  implicit val trackFormat: RootJsonFormat[Track] = jsonFormat3(Track)
  implicit val playlistItemFormat: RootJsonFormat[PlaylistItem] = jsonFormat1(PlaylistItem)
  implicit val playlistTracksFormat: RootJsonFormat[PlaylistTracks] = jsonFormat1(PlaylistTracks)

  // Initialize HTTP client
  implicit val backend = HttpURLConnectionBackend()

  def getPlaylistData(): List[Track] = {
    val playlistRequest = basicRequest
      .get(uri"https://api.spotify.com/v1/playlists/$playlistId/tracks")
      .header("Authorization", s"Bearer $accessToken")

    val response = playlistRequest.send(backend)

    response.body match {
      case Right(body) =>
        body.parseJson.convertTo[PlaylistTracks].items.map(_.track)
      case Left(error) =>
        println(s"Error fetching playlist")
        List.empty
    }
  }

  def getArtistData(artistId: String): Option[ArtistDetails] = {
    val artistRequest = basicRequest
      .get(uri"https://api.spotify.com/v1/artists/$artistId")
      .header("Authorization", s"Bearer $accessToken")

    val response = artistRequest.send(backend)

    response.body match {
      case Right(body) =>
        Some(body.parseJson.convertTo[ArtistDetails])
      case Left(error) =>
        println(s"Error fetching artist")
        None
    }
  }

  // Find top 10 longest
  val tracks = getPlaylistData()
  val top10LongestTracks = tracks.sortBy(-_.duration_ms).take(10)

  println("\nTop 10 Longest Songs:")
  top10LongestTracks.foreach { track =>
  println(f"${track.name}, ${track.duration_ms}ms")
  }

  // Find unique artists and their followers
  val uniqueArtists = top10LongestTracks.flatMap(_.artists).distinctBy(_.id)
  val artistDetails = uniqueArtists.flatMap { artist =>
    getArtistData(artist.id).map(details => (artist.name, details.followers.total))
  }

  println("\nArtists by Follower Count:")
  artistDetails.sortBy(-_._2).foreach {
    case (name, followers) => val followersFormatted = followers.toString.reverse.grouped(3).mkString(",").reverse
    println(s"$name: $followersFormatted")
  }
}
