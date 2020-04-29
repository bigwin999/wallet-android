/**
* Auth API
* Auth API<br> <a href='/changelog'>Changelog</a>
*
* The version of the OpenAPI document: v0.0.50
* 
*
* NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
* https://openapi-generator.tech
* Do not edit the class manually.
*/
package com.mycelium.bequant.remote.client.models



import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 * @param newPassword 
 * @param oldPassword 
 */
@JsonClass(generateAdapter = true)
data class AccountPasswordUpdateRequest (
    @Json(name = "new_password")
    val newPassword: kotlin.String,
    @Json(name = "old_password")
    val oldPassword: kotlin.String
)

