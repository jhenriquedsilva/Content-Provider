package com.example.contentprovider.database

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.media.UnsupportedSchemeException
import android.net.Uri
import android.provider.BaseColumns._ID
import com.example.contentprovider.database.NotesDatabaseHelper.Companion.TABLE_NOTES

class NotesProvider : ContentProvider() {

    // Validação da url de requisição do Content Provider
    private lateinit var mUriMatcher: UriMatcher
    private lateinit var dbHelper: NotesDatabaseHelper

    override fun onCreate(): Boolean {
        // Instaciando-o vazio
        mUriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        // Endereços e identificações do COntent Provider
        mUriMatcher.addURI(AUTHORITY,"notes", NOTES)
        mUriMatcher.addURI(AUTHORITY,"notes/#", NOTES_BY_ID)

        // Instanciando o banco de dados
        if (context != null) { dbHelper = NotesDatabaseHelper(context as Context) }

        return true
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {

        if (mUriMatcher.match(uri) == NOTES_BY_ID) {
            val db: SQLiteDatabase = dbHelper.writableDatabase
            val linesAffected = db.delete(TABLE_NOTES,"$_ID =?", arrayOf(uri.lastPathSegment))
            db.close()
            context?.contentResolver?.notifyChange(uri, null)
            return linesAffected
        } else {
            throw UnsupportedSchemeException("Uri inválida para exclusão!")
        }
    }

    override fun getType(uri: Uri) = throw UnsupportedSchemeException("Uri não implementado")

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if (mUriMatcher.match(uri) == NOTES) {
            val db: SQLiteDatabase = dbHelper.writableDatabase
            val id = db.insert(TABLE_NOTES,null,values)
            var insertUri = Uri.withAppendedPath(BASE_URI,id.toString())
            db.close()
            context?.contentResolver?.notifyChange(uri,null)
            return insertUri
        } else {
            throw UnsupportedSchemeException("Uri inválida para inserção!")
        }
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        return when {
            mUriMatcher.match(uri) == NOTES -> {
                val db: SQLiteDatabase = dbHelper.writableDatabase
                val cursor = db.query(TABLE_NOTES,projection,selection,selectionArgs,null,null,sortOrder)
                cursor.setNotificationUri(context?.contentResolver,uri)
                cursor
            }
            mUriMatcher.match(uri) == NOTES_BY_ID -> {
                val db: SQLiteDatabase = dbHelper.writableDatabase
                val cursor = db.query(TABLE_NOTES,projection,"$_ID = ?",
                    arrayOf(uri.lastPathSegment),null,null,sortOrder)
                cursor.setNotificationUri(context?.contentResolver,uri)
                cursor
            }
            else -> throw UnsupportedSchemeException("Uri não implementada!")
        }
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        if (mUriMatcher.match(uri) == NOTES_BY_ID) {
            val db: SQLiteDatabase = dbHelper.writableDatabase
            val linesAffected = db.update(TABLE_NOTES,values,"$_ID = ?", arrayOf(uri.lastPathSegment))
            db.close()
            context?.contentResolver?.notifyChange(uri,null)
            return linesAffected
        } else {
            throw UnsupportedSchemeException("Uri não implementada!")
        }
    }

    companion object {
        // Endereço do Content Provider
        const val AUTHORITY = "com.example.contentprovider.provider"
        val BASE_URI = Uri.parse("content://$AUTHORITY") // Converte para URI a string
        val URI_NOTES = Uri.withAppendedPath(BASE_URI,"notes") // Nomeando uma url notas
        const val NOTES = 1
        const val NOTES_BY_ID = 2
    }
}