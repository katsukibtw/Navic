package paige.navic.data.repository

import paige.navic.data.session.SessionManager
import paige.subsonic.api.model.Album
import paige.subsonic.api.model.ListType

open class AlbumsRepository {
	open suspend fun getAlbums(
		listType: ListType = ListType.ALPHABETICAL_BY_ARTIST
	): List<Album> {
		return SessionManager.api
			.getAlbumList(type = listType, size = 500)
			.data.albumList.album.orEmpty().map { album ->
				album.copy(
					coverArt = SessionManager.api
						.getCoverArtUrl(album.coverArt, size = 512, auth = true)
				)
			}
	}
	suspend fun isAlbumStarred(album: Album): Boolean? {
		return SessionManager.api.getStarred()
				.data.starred.album
				?.map { it.id }
				?.contains(album.id)
	}
	suspend fun starAlbum(album: Album) {
		SessionManager.api.star(listOf(album.id))
	}
	suspend fun unstarAlbum(album: Album) {
		SessionManager.api.unstar(listOf(album.id))
	}
}
