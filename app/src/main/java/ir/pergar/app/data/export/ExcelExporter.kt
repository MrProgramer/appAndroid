package ir.pergar.app.data.export

import android.content.Context
import android.net.Uri
import ir.pergar.app.data.db.Account
import ir.pergar.app.data.db.Category
import ir.pergar.app.data.db.TxnEntity
import ir.pergar.app.data.model.TxnSource
import ir.pergar.app.data.model.TxnType
import ir.pergar.app.data.util.formatJalaliNumeric
import java.io.BufferedOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ExcelExporter {

    private const val HEADER_STYLE = 1
    private const val NUMBER_STYLE = 2

    fun export(
        context: Context,
        uri: Uri,
        txns: List<TxnEntity>,
        categories: List<Category>,
        accounts: List<Account>
    ): Int {
        val categoriesById = categories.associateBy { it.id }
        val accountsById = accounts.associateBy { it.id }
        val sorted = txns.sortedByDescending { it.dateEpochMillis }

        val output = context.contentResolver.openOutputStream(uri)
            ?: throw IOException("امکان نوشتن فایل وجود ندارد")
        ZipOutputStream(BufferedOutputStream(output)).use { zip ->
            writeEntry(zip, "[Content_Types].xml", contentTypes())
            writeEntry(zip, "_rels/.rels", rootRels())
            writeEntry(zip, "xl/workbook.xml", workbook())
            writeEntry(zip, "xl/_rels/workbook.xml.rels", workbookRels())
            writeEntry(zip, "xl/styles.xml", styles())
            writeEntry(zip, "xl/worksheets/sheet1.xml", sheet(sorted, categoriesById, accountsById))
        }
        return sorted.size
    }

    private fun writeEntry(zip: ZipOutputStream, name: String, content: String) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(content.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun sheet(
        txns: List<TxnEntity>,
        categories: Map<Long, Category>,
        accounts: Map<Long, Account>
    ): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
        sb.append("""<sheetViews><sheetView workbookViewId="0" rightToLeft="1"/></sheetViews>""")
        sb.append("""<sheetFormatPr defaultRowHeight="19"/>""")
        sb.append("""<cols>""")
        sb.append("""<col min="1" max="1" width="14" customWidth="1"/>""")
        sb.append("""<col min="2" max="2" width="11" customWidth="1"/>""")
        sb.append("""<col min="3" max="3" width="16" customWidth="1"/>""")
        sb.append("""<col min="4" max="4" width="20" customWidth="1"/>""")
        sb.append("""<col min="5" max="5" width="20" customWidth="1"/>""")
        sb.append("""<col min="6" max="6" width="24" customWidth="1"/>""")
        sb.append("""<col min="7" max="7" width="44" customWidth="1"/>""")
        sb.append("""<col min="8" max="8" width="12" customWidth="1"/>""")
        sb.append("</cols><sheetData>")

        sb.append("<row r=\"1\">")
        listOf("تاریخ", "نوع", "مبلغ (تومان)", "دسته", "حساب", "فروشنده", "توضیحات", "منبع")
            .forEachIndexed { index, header ->
                sb.append(stringCell(index, 1, header, HEADER_STYLE))
            }
        sb.append("</row>")

        txns.forEachIndexed { index, txn ->
            val row = index + 2
            val typeLabel = when (txn.type) {
                TxnType.INCOME -> "درآمد"
                TxnType.EXPENSE -> "هزینه"
                TxnType.TRANSFER -> "انتقال"
            }
            val categoryName = txn.categoryId?.let { categories[it]?.name } ?: "-"
            val accountName = txn.accountId?.let { accounts[it]?.name } ?: "-"
            val sourceLabel = when (txn.source) {
                TxnSource.MANUAL -> "دستی"
                TxnSource.SMS -> "پیامک"
                TxnSource.VOICE -> "صدا"
                TxnSource.AUTO -> "خودکار"
            }
            sb.append("<row r=\"$row\">")
            sb.append(stringCell(0, row, formatJalaliNumeric(txn.dateEpochMillis)))
            sb.append(stringCell(1, row, typeLabel))
            sb.append(numberCell(2, row, txn.amountRial / 10))
            sb.append(stringCell(3, row, categoryName))
            sb.append(stringCell(4, row, accountName))
            sb.append(stringCell(5, row, txn.merchant))
            sb.append(stringCell(6, row, txn.note))
            sb.append(stringCell(7, row, sourceLabel))
            sb.append("</row>")
        }

        sb.append("</sheetData></worksheet>")
        return sb.toString()
    }

    private fun stringCell(col: Int, row: Int, value: String, style: Int = 0): String {
        val ref = "${colLetter(col)}$row"
        val styleAttr = if (style > 0) " s=\"$style\"" else ""
        return "<c r=\"$ref\"$styleAttr t=\"inlineStr\"><is><t xml:space=\"preserve\">${escape(value)}</t></is></c>"
    }

    private fun numberCell(col: Int, row: Int, value: Long): String {
        val ref = "${colLetter(col)}$row"
        return "<c r=\"$ref\" s=\"$NUMBER_STYLE\"><v>$value</v></c>"
    }

    private fun colLetter(index: Int): String {
        var i = index
        val sb = StringBuilder()
        while (i >= 0) {
            sb.insert(0, ('A' + (i % 26)))
            i = i / 26 - 1
        }
        return sb.toString()
    }

    private fun escape(value: String): String = value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("\n", " ")

    private fun contentTypes(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
<Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>"""

    private fun rootRels(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

    private fun workbook(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
<sheets><sheet name="تراکنش‌ها" sheetId="1" r:id="rId1"/></sheets>
</workbook>"""

    private fun workbookRels(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""

    private fun styles(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<numFmts count="1"><numFmt numFmtId="164" formatCode="#,##0"/></numFmts>
<fonts count="2">
<font><sz val="11"/><name val="Vazirmatn"/></font>
<font><b/><sz val="11"/><color rgb="FFFFFFFF"/><name val="Vazirmatn"/></font>
</fonts>
<fills count="3">
<fill><patternFill patternType="none"/></fill>
<fill><patternFill patternType="gray125"/></fill>
<fill><patternFill patternType="solid"><fgColor rgb="FF1D4ED8"/><bgColor indexed="64"/></patternFill></fill>
</fills>
<borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders>
<cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
<cellXfs count="3">
<xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
<xf numFmtId="0" fontId="1" fillId="2" borderId="0" xfId="0" applyFont="1" applyFill="1"/>
<xf numFmtId="164" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1"/>
</cellXfs>
<cellStyles count="1"><cellStyle name="Normal" xfId="0" builtinId="0"/></cellStyles>
</styleSheet>"""
}
