package com.sakethh.linkora.ui.components.link


import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakethh.linkora.preferences.AppPreferences
import com.sakethh.linkora.utils.host
import com.sakethh.linkora.utils.getVideoPlatformBaseUrls
import com.sakethh.linkora.domain.MediaType
import com.sakethh.linkora.ui.components.CoilImage
import com.sakethh.linkora.ui.domain.model.LinkUIComponentParam
import com.sakethh.linkora.ui.utils.fadedEdges
import com.sakethh.linkora.ui.utils.pressScaleEffect

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GridViewLinkUIComponent(
    linkUIComponentParam: LinkUIComponentParam,
    forStaggeredView: Boolean,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        colors = CardDefaults.cardColors(containerColor = if (linkUIComponentParam.isSelectionModeEnabled.value) colorScheme.primaryContainer else CardDefaults.cardColors().containerColor),
        modifier = Modifier.fillMaxWidth().animateContentSize()
            .then(if (!forStaggeredView) Modifier.wrapContentHeight() else Modifier)
            .pointerHoverIcon(icon = PointerIcon.Hand)
            .combinedClickable(onClick = {
                if (!linkUIComponentParam.isSelectionModeEnabled.value) {
                    linkUIComponentParam.onMoreIconClick()
                    return@combinedClickable
                }
                linkUIComponentParam.onLinkClick()
            }, interactionSource = remember {
                MutableInteractionSource()
            }, indication = null, onLongClick = {
                linkUIComponentParam.onLongClick()
            }).pressScaleEffect().padding(4.dp).then(modifier)
    ) {
        if (linkUIComponentParam.isItemSelected.value) {
            Box(
                Modifier.fillMaxWidth().height(150.dp)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = colorScheme.onPrimary)
            }
        } else if (linkUIComponentParam.link.imgURL.trim().isNotBlank()) {
            Box(modifier = if (forStaggeredView) Modifier.fillMaxSize() else Modifier.height(150.dp)) {
                CoilImage(
                    modifier = Modifier.fillMaxSize().then(
                        if (AppPreferences.enableFadedEdgeForNonListViews.value) Modifier.fadedEdges(
                            colorScheme
                        ) else Modifier
                    ),
                    imgURL = linkUIComponentParam.link.imgURL,
                    contentScale = if (linkUIComponentParam.link.imgURL.startsWith("https://pbs.twimg.com/profile_images/") || !AppPreferences.isShelfMinimizedInHomeScreen.value || !forStaggeredView) ContentScale.Crop else ContentScale.Fit,
                    userAgent = linkUIComponentParam.link.userAgent
                        ?: AppPreferences.primaryJsoupUserAgent.value
                )
                if (AppPreferences.showVideoTagOnUIIfApplicable.value && (linkUIComponentParam.link.mediaType == MediaType.VIDEO || linkUIComponentParam.link.url.host(
                        throwOnException = false
                    ) in getVideoPlatformBaseUrls())
                ) {
                    Text(
                        text = MediaType.VIDEO.name,
                        modifier = Modifier.padding(
                            start = 10.dp,
                        )
                            .padding(bottom = if (AppPreferences.enableBaseURLForLinkViews.value || (!AppPreferences.enableBaseURLForLinkViews.value && !AppPreferences.enableTitleForNonListViews.value)) 10.dp else 0.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondary.copy(0.25f),
                                shape = RoundedCornerShape(5.dp)
                            ).padding(5.dp).align(Alignment.BottomStart),
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 8.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        if (AppPreferences.enableTitleForNonListViews.value) {
            Text(
                text = linkUIComponentParam.link.title,
                modifier = Modifier.padding(
                    start = 10.dp,
                    top = 10.dp,
                    end = 10.dp,
                    bottom = if (linkUIComponentParam.isSelectionModeEnabled.value || !AppPreferences.enableBaseURLForLinkViews.value) 10.dp else 0.dp
                ),
                style = MaterialTheme.typography.titleSmall,
                overflow = TextOverflow.Ellipsis,
                fontSize = 15.sp,
                maxLines = 3
            )
        }
        if (AppPreferences.enableNoteForNonListViews.value && linkUIComponentParam.link.note.isNotBlank()) {
            Text(
                text = linkUIComponentParam.link.note,
                modifier = Modifier.padding(
                    start = 10.dp,
                    top = 10.dp,
                    end = 10.dp,
                    bottom = if (linkUIComponentParam.isSelectionModeEnabled.value || !AppPreferences.enableBaseURLForLinkViews.value) 10.dp else 0.dp
                ),
                style = MaterialTheme.typography.titleSmall,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp,
                maxLines = 3,
                color = MaterialTheme.colorScheme.onSurface.copy(0.75f)
            )
        }
        if (!linkUIComponentParam.isSelectionModeEnabled.value && AppPreferences.enableBaseURLForLinkViews.value) {
            Text(
                text = linkUIComponentParam.link.url.host(throwOnException = false),
                modifier = Modifier.padding(10.dp).background(
                    color = MaterialTheme.colorScheme.primary.copy(0.25f),
                    shape = RoundedCornerShape(5.dp)
                ).padding(5.dp),
                style = MaterialTheme.typography.titleLarge,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}