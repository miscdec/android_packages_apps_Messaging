package com.android.messaging2.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.graphics.drawable.IconCompat

object BubbleUtils {


    @RequiresApi(Build.VERSION_CODES.R)
    fun createBubble(
        context: Context,
        targetIntent: PendingIntent,
        icon: IconCompat
    ): NotificationCompat.BubbleMetadata {
        // Create bubble intent
//        val target = Intent(context, LaunchConversationActivity::class.java)
//        val category = "com.example.category.IMG_SHARE_TARGET"

//        val chatPartner = Person.Builder()
//            .setName("Chat partner")
//            .setImportant(true)
//            .build()


// Create bubble metadata

        return NotificationCompat.BubbleMetadata.Builder(targetIntent, icon)
            .setAutoExpandBubble(true)
            .setSuppressNotification(true)
            .setDesiredHeight(600)
            .build()

// Create notification, referencing the sharing shortcut
//        val builder = Notification.Builder(context, CHANNEL_ID)
//            .setContentIntent(contentIntent)
//            .setSmallIcon(smallIcon)
//            .setBubbleMetadata(bubbleData)
//            .setShortcutId(shortcutId)
//            .addPerson(chatPartner)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun createShortcut(
        context: Context,
        shortcutId: String,
        shortLabel: String,
        categories: Set<String>
    ): ShortcutInfoCompat {
        // Create sharing shortcut
        return ShortcutInfoCompat.Builder(context, shortcutId)
            .setCategories(categories)
            .setIntent(Intent(Intent.ACTION_DEFAULT))
            .setLongLived(true)
            .setShortLabel(shortLabel)
            .build()
    }
}
