package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BalanceWidgetProvider : AppWidgetProvider {
    constructor() : super()

    companion object {
        const val ACTION_REFRESH = "com.example.widget.ACTION_REFRESH"

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            balance: Double = 0.0,
            todayIncome: Double = 0.0,
            todayExpense: Double = 0.0
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_balance)
            val formatter = DecimalFormat("#,##0.00")
            val shortFormatter = DecimalFormat("#,##0")

            views.setTextViewText(R.id.widget_tv_balance, "৳ ${formatter.format(balance)}")
            views.setTextViewText(R.id.widget_tv_income, "+৳ ${shortFormatter.format(todayIncome)}")
            views.setTextViewText(R.id.widget_tv_expense, "-৳ ${shortFormatter.format(todayExpense)}")

            // Intent to open app
            val appIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val appPendingIntent = PendingIntent.getActivity(
                context,
                0,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, appPendingIntent)

            // Intent to refresh
            val refreshIntent = Intent(context, BalanceWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun refreshAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, BalanceWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            if (allWidgetIds.isEmpty()) return

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val auth = FirebaseAuth.getInstance()
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        val db = FirebaseFirestore.getInstance()
                        val snapshot = db.collection("users").document(uid).collection("transactions").get()
                        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                        var totalIncome = 0.0
                        var totalExpense = 0.0
                        var todayInc = 0.0
                        var todayExp = 0.0

                        snapshot.addOnSuccessListener { querySnap ->
                            for (doc in querySnap.documents) {
                                val amount = doc.getDouble("amount") ?: 0.0
                                val type = doc.getString("type") ?: ""
                                val date = doc.getString("date") ?: ""

                                if (type == "INCOME") {
                                    totalIncome += amount
                                    if (date == todayStr) todayInc += amount
                                } else {
                                    totalExpense += amount
                                    if (date == todayStr) todayExp += amount
                                }
                            }
                            val netBalance = totalIncome - totalExpense
                            for (id in allWidgetIds) {
                                updateWidget(context, appWidgetManager, id, netBalance, todayInc, todayExp)
                            }
                        }
                    } else {
                        for (id in allWidgetIds) {
                            updateWidget(context, appWidgetManager, id, 0.0, 0.0, 0.0)
                        }
                    }
                } catch (e: Exception) {
                    for (id in allWidgetIds) {
                        updateWidget(context, appWidgetManager, id, 0.0, 0.0, 0.0)
                    }
                }
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        refreshAllWidgets(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            refreshAllWidgets(context)
        }
    }
}
