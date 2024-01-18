package suwayomi.tachidesk.manga.model.dataclass

/**
 * @author mc
 */

fun buildImgDataClass(url: String, headers: Map<String, String>?): ImgDataClass {
    return ImgDataClass(url = url, headers = headers)
}

data class ImgDataClass(
    val url: String,
    val method: String? = "GET",
    val headers: Map<String, String>?
)
