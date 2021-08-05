package com.example.contentprovider

import android.database.Cursor

// Gerenciar os clicks no Adapter
interface NoteClickedListener {
    fun noteClickedItem(cursor: Cursor)
    fun noteRemoveItem(cursor: Cursor?)
}