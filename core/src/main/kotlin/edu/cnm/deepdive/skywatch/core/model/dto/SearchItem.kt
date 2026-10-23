package edu.cnm.deepdive.skywatch.core.model.dto

import java.net.URL

data class SearchItem(
    val mediaCollectionUrl: URL,
    val data: Array<ItemData>,
    val links: Array<Link>
)
