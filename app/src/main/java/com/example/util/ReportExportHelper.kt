package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExportHelper {

    fun exportToCsv(
        context: Context,
        periodName: String,
        transactions: List<Transaction>
    ): File? {
        return try {
            val dir = File(context.cacheDir, "reports").apply { mkdirs() }
            val fileName = "HisabKhata_${System.currentTimeMillis()}.csv"
            val file = File(dir, fileName)

            FileOutputStream(file).use { out ->
                // Write UTF-8 BOM so Excel decodes Bengali properly
                out.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))

                val writer = out.bufferedWriter(Charsets.UTF_8)
                writer.write("তারিখ,বিবরণ,ধরণ,ক্যাটাগরি,ওয়ালেট,পরিমাণ (টাকা),নোট\n")

                val decimalFormat = DecimalFormat("0.00")
                for (tx in transactions) {
                    val date = tx.date
                    val title = escapeCsv(tx.title)
                    val type = if (tx.type == TransactionType.INCOME) "আয়" else "ব্যয়"
                    val cat = escapeCsv(tx.categoryName)
                    val wallet = escapeCsv(tx.walletName)
                    val amount = decimalFormat.format(tx.amount)
                    val note = escapeCsv(tx.note)
                    writer.write("$date,$title,$type,$cat,$wallet,$amount,$note\n")
                }
                writer.flush()
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun escapeCsv(text: String): String {
        val clean = text.replace("\"", "\"\"")
        return if (clean.contains(",") || clean.contains("\n") || clean.contains("\"")) {
            "\"$clean\""
        } else {
            clean
        }
    }

    fun exportToPdf(
        context: Context,
        periodTitle: String,
        transactions: List<Transaction>,
        totalIncome: Double,
        totalExpense: Double,
        netBalance: Double
    ): File? {
        return try {
            val pdfDoc = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            var pageNumber = 1

            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDoc.startPage(pageInfo)
            var canvas = page.canvas

            val paint = Paint().apply { isAntiAlias = true }
            val df = DecimalFormat("#,##0.00")

            // Header Background Banner
            paint.color = Color.parseColor("#0B4734")
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 90f, paint)

            // App Title in Banner
            paint.color = Color.WHITE
            paint.textSize = 22f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("হিসাব-খাতা আর্থিক প্রতিবেদন", 24f, 42f, paint)

            // Subtitle / Period
            paint.textSize = 12f
            paint.typeface = Typeface.DEFAULT
            paint.color = Color.parseColor("#C2EBD4")
            val generatedDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
            canvas.drawText("সময়কাল: $periodTitle  •  তৈরির সময়: $generatedDate", 24f, 66f, paint)

            // Summary Cards Box
            var yPos = 110f
            val cardWidth = (pageWidth - 48 - 20) / 3f

            // Income Card
            drawStatCard(canvas, 24f, yPos, cardWidth, 60f, "মোট আয়", "+৳ ${df.format(totalIncome)}", Color.parseColor("#E6F7ED"), Color.parseColor("#0B6E4F"))
            // Expense Card
            drawStatCard(canvas, 24f + cardWidth + 10, yPos, cardWidth, 60f, "মোট ব্যয়", "-৳ ${df.format(totalExpense)}", Color.parseColor("#FCEEEE"), Color.parseColor("#BA1A1A"))
            // Net Balance Card
            val balBg = if (netBalance >= 0) Color.parseColor("#EAF4FE") else Color.parseColor("#FFF3E0")
            val balFg = if (netBalance >= 0) Color.parseColor("#0D47A1") else Color.parseColor("#E65100")
            drawStatCard(canvas, 24f + (cardWidth + 10) * 2, yPos, cardWidth, 60f, "মোট স্থিতি / ব্যালেন্স", "৳ ${df.format(netBalance)}", balBg, balFg)

            yPos += 80f

            // Table Header
            paint.color = Color.parseColor("#E8F0EA")
            canvas.drawRect(24f, yPos, pageWidth - 24f, yPos + 26f, paint)

            paint.color = Color.parseColor("#1B221E")
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("তারিখ", 30f, yPos + 18f, paint)
            canvas.drawText("বিবরণ", 110f, yPos + 18f, paint)
            canvas.drawText("ক্যাটাগরি", 270f, yPos + 18f, paint)
            canvas.drawText("ওয়ালেট", 380f, yPos + 18f, paint)
            canvas.drawText("পরিমাণ (টাকা)", 480f, yPos + 18f, paint)

            yPos += 32f
            paint.typeface = Typeface.DEFAULT

            val maxRowsPerPage = 26
            var rowCountOnPage = 0

            for (tx in transactions) {
                if (rowCountOnPage >= maxRowsPerPage) {
                    pdfDoc.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDoc.startPage(pageInfo)
                    canvas = page.canvas
                    yPos = 50f
                    rowCountOnPage = 0

                    // Re-draw Table Header on new page
                    paint.color = Color.parseColor("#E8F0EA")
                    canvas.drawRect(24f, yPos, pageWidth - 24f, yPos + 26f, paint)

                    paint.color = Color.parseColor("#1B221E")
                    paint.textSize = 11f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText("তারিখ", 30f, yPos + 18f, paint)
                    canvas.drawText("বিবরণ", 110f, yPos + 18f, paint)
                    canvas.drawText("ক্যাটাগরি", 270f, yPos + 18f, paint)
                    canvas.drawText("ওয়ালেট", 380f, yPos + 18f, paint)
                    canvas.drawText("পরিমাণ (টাকা)", 480f, yPos + 18f, paint)

                    yPos += 32f
                    paint.typeface = Typeface.DEFAULT
                }

                // Row Divider Line
                paint.color = Color.parseColor("#EEEEEE")
                canvas.drawLine(24f, yPos + 14f, pageWidth - 24f, yPos + 14f, paint)

                paint.color = Color.parseColor("#333333")
                paint.textSize = 10f
                canvas.drawText(tx.date, 30f, yPos + 8f, paint)

                val shortTitle = if (tx.title.length > 24) tx.title.take(22) + ".." else tx.title
                canvas.drawText(shortTitle, 110f, yPos + 8f, paint)

                val shortCat = if (tx.categoryName.length > 16) tx.categoryName.take(14) + ".." else tx.categoryName
                canvas.drawText(shortCat, 270f, yPos + 8f, paint)

                val shortWallet = if (tx.walletName.length > 14) tx.walletName.take(12) + ".." else tx.walletName
                canvas.drawText(shortWallet, 380f, yPos + 8f, paint)

                // Amount
                val isIncome = tx.type == TransactionType.INCOME
                paint.color = if (isIncome) Color.parseColor("#0B6E4F") else Color.parseColor("#BA1A1A")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                val sign = if (isIncome) "+৳ " else "-৳ "
                canvas.drawText("$sign${df.format(tx.amount)}", 480f, yPos + 8f, paint)
                paint.typeface = Typeface.DEFAULT

                yPos += 22f
                rowCountOnPage++
            }

            // Page Footer
            paint.color = Color.parseColor("#888888")
            paint.textSize = 9f
            canvas.drawText("পৃষ্ঠা $pageNumber  •  হিসাব-খাতা অ্যাপ দ্বারা প্রস্তুতকৃত", 24f, pageHeight - 20f, paint)

            pdfDoc.finishPage(page)

            val dir = File(context.cacheDir, "reports").apply { mkdirs() }
            val fileName = "HisabKhata_Report_${System.currentTimeMillis()}.pdf"
            val file = File(dir, fileName)
            FileOutputStream(file).use { out ->
                pdfDoc.writeTo(out)
            }
            pdfDoc.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun drawStatCard(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        title: String,
        value: String,
        bgColor: Int,
        fgColor: Int
    ) {
        val paint = Paint().apply { isAntiAlias = true }
        paint.color = bgColor
        canvas.drawRoundRect(x, y, x + width, y + height, 8f, 8f, paint)

        paint.color = Color.parseColor("#555555")
        paint.textSize = 9.5f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(title, x + 10f, y + 20f, paint)

        paint.color = fgColor
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(value, x + 10f, y + 42f, paint)
    }

    fun shareFile(context: Context, file: File, mimeType: String, title: String = "রিপোর্ট শেয়ার করুন") {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
