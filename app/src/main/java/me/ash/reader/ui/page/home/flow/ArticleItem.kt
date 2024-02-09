package me.ash.reader.ui.page.home.flow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.size.Precision
import coil.size.Scale
import me.ash.reader.R
import me.ash.reader.domain.model.article.ArticleWithFeed
import me.ash.reader.infrastructure.preference.*
import me.ash.reader.ui.component.FeedIcon
import me.ash.reader.ui.component.base.RYAsyncImage
import me.ash.reader.ui.component.base.SIZE_1000
import me.ash.reader.ui.ext.surfaceColorAtElevation
import me.ash.reader.ui.theme.Shape20
import me.ash.reader.ui.theme.palette.onDark

@Composable
fun ArticleItem(
    articleWithFeed: ArticleWithFeed,
    onClick: (ArticleWithFeed) -> Unit = {},
) {
    val articleListFeedIcon = LocalFlowArticleListFeedIcon.current
    val articleListFeedName = LocalFlowArticleListFeedName.current
    val articleListImage = LocalFlowArticleListImage.current
    val articleListDesc = LocalFlowArticleListDesc.current
    val articleListDate = LocalFlowArticleListTime.current
    val articleListReadIndicator = LocalFlowArticleListReadIndicator.current

    Column(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .clip(Shape20)
            .clickable { onClick(articleWithFeed) }
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .alpha(
                articleWithFeed.article.run {
                    when (articleListReadIndicator) {
                        FlowArticleReadIndicatorPreference.AllRead -> {
                            if (isUnread) 1f else 0.5f
                        }

                        FlowArticleReadIndicatorPreference.ExcludingStarred -> {
                            if (isUnread || isStarred) 1f else 0.5f
                        }
                    }
                }
            ),
    ) {
        // Top
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Feed name
            if (articleListFeedName.value) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = if (articleListFeedIcon.value) 30.dp else 0.dp),
                    text = articleWithFeed.feed.name,
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Right
            if (articleListDate.value) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!articleListFeedName.value) {
                        Spacer(Modifier.width(if (articleListFeedIcon.value) 30.dp else 0.dp))
                    }
                    // Starred
                    if (articleWithFeed.article.isStarred) {
                        Icon(
                            modifier = Modifier
                                .size(14.dp)
                                .padding(end = 2.dp),
                            imageVector = Icons.Rounded.Star,
                            contentDescription = stringResource(R.string.starred),
                            tint = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }

                    // Date
                    Text(
                        modifier = Modifier,
                        text = articleWithFeed.article.dateString ?: "",
                        color = MaterialTheme.colorScheme.outlineVariant,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }

        // Bottom
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        ) {
            // Feed icon
            if (articleListFeedIcon.value) {
                FeedIcon(articleWithFeed.feed.name, iconUrl = articleWithFeed.feed.icon)
                Spacer(modifier = Modifier.width(10.dp))
            }

            // Article
            Column(
                modifier = Modifier.weight(1f),
            ) {

                // Title
                Text(
                    text = articleWithFeed.article.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = if (articleListDesc.value) 2 else 4,
                    overflow = TextOverflow.Ellipsis,
                )

                // Description
                if (articleListDesc.value && articleWithFeed.article.shortDescription.isNotBlank()) {
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = articleWithFeed.article.shortDescription,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Image
            if (articleWithFeed.article.img != null && articleListImage.value) {
                RYAsyncImage(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .size(80.dp)
                        .clip(Shape20),
                    data = articleWithFeed.article.img,
                    scale = Scale.FILL,
                    precision = Precision.INEXACT,
                    size = SIZE_1000,
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun SwipeableArticleItem(
    articleWithFeed: ArticleWithFeed,
    isFilterUnread: Boolean,
    articleListTonalElevation: Int,
    onClick: (ArticleWithFeed) -> Unit = {},
    onSwipeOut: (ArticleWithFeed) -> Unit = {},
) {
    val articleSwipeDirectionPreference = LocalFlowArticleSwipeDirection.current
    var isArticleVisible by remember { mutableStateOf(true) }
    val dismissState =
        rememberDismissState(initialValue = DismissValue.Default, confirmStateChange = {
            val startToEndSwipe = articleSwipeDirectionPreference.isStartToEnd() && it == DismissValue.DismissedToEnd
            val endToStartSwipe = articleSwipeDirectionPreference.isEndToStart() && it == DismissValue.DismissedToStart

            if (startToEndSwipe || endToStartSwipe) {
                isArticleVisible = !isFilterUnread
                onSwipeOut(articleWithFeed)
            }
            isFilterUnread
        })
    if (isArticleVisible) {
        SwipeToDismiss(
            state = dismissState,
            /***  create dismiss alert background box */
            background = {
                Row {
                    if (articleSwipeDirectionPreference.isStartToEnd()) {
                        MarkAsReadSwipeAction()
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (articleSwipeDirectionPreference.isEndToStart()) {
                        MarkAsReadSwipeAction()
                    }
                }
            },
            /**** Dismiss Content */
            dismissContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.surfaceColorAtElevation(
                                articleListTonalElevation.dp
                            ) onDark MaterialTheme.colorScheme.surface
                        )
                ) {
                    ArticleItem(articleWithFeed, onClick)
                }
            },
            /*** Set Direction to dismiss */
            directions = when (articleSwipeDirectionPreference) {
                FlowArticleSwipeDirectionPreference.None -> emptySet()
                FlowArticleSwipeDirectionPreference.StartToEnd -> setOf(DismissDirection.StartToEnd)
                FlowArticleSwipeDirectionPreference.EndToStart -> setOf(DismissDirection.EndToStart)
                FlowArticleSwipeDirectionPreference.Both -> setOf(
                    DismissDirection.StartToEnd,
                    DismissDirection.EndToStart,
                )
            },
        )
    }
}

@Composable
private fun SwipeAction(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    text: String,
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            // .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = text,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.labelLarge,
            )
        }

    }
}

@Composable
private fun MarkAsReadSwipeAction() {
    SwipeAction(
        icon = Icons.Rounded.CheckCircleOutline,
        text = stringResource(R.string.mark_as_read)
    )
}
