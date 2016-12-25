package com.dtalk.dd.DB.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.dtalk.dd.DB.entity.GifEmoEntity;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

/**
 * Created by Donal on 2016/12/21.
 */

public class GifEmoDao extends AbstractDao<GifEmoEntity, Long> {

    public static final String TABLENAME = "GifEmo";

    /**
     * Properties of entity DepartmentEntity.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", false, "_id");
        public final static Property Url = new Property(1, String.class, "url", false, "URL");
        public final static Property Path = new Property(2, String.class, "path", false, "PATH");
        public final static Property Mean = new Property(3, String.class, "mean", false, "MEAN");
        public final static Property Type = new Property(4, int.class, "type", false, "TYPE");
    }

    ;

    public GifEmoDao(DaoConfig config) {
        super(config);
    }

    public GifEmoDao(DaoConfig config, AbstractDaoSession daoSession) {
        super(config, daoSession);
    }

    /**
     * Creates the underlying database table.
     */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists ? "IF NOT EXISTS " : "";
        db.execSQL("CREATE TABLE " + constraint + "'GifEmo' (" + //
                "'_id' INTEGER NOT NULL PRIMARY KEY," +
                "'URL' TEXT NOT NULL UNIQUE," +
                "'PATH' TEXT NOT NULL ," +
                "'MEAN' TEXT NOT NULL ," +
                "'TYPE' INTEGER NOT NULL );");
    }

    /**
     * Drops the underlying database table.
     */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'GifEmo'";
        db.execSQL(sql);
    }

    @Override
    protected GifEmoEntity readEntity(Cursor cursor, int offset) {
        GifEmoEntity entity = new GifEmoEntity( //
                cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
                cursor.getString(offset + 1),
                cursor.getString(offset + 2),
                cursor.getString(offset + 3),
                cursor.getInt(offset + 4)
        );
        return entity;
    }

    @Override
    protected Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }

    @Override
    protected void readEntity(Cursor cursor, GifEmoEntity entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setUrl(cursor.getString(offset + 1));
        entity.setPath(cursor.getString(offset + 2));
        entity.setMean(cursor.getString(offset + 3));
        entity.setType(cursor.getInt(offset + 4));
    }

    @Override
    protected void bindValues(SQLiteStatement stmt, GifEmoEntity entity) {
        stmt.clearBindings();

        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getUrl());
        stmt.bindString(3, entity.getPath());
        stmt.bindString(4, entity.getMean());
        stmt.bindLong(5, entity.getType());
    }

    @Override
    protected Long updateKeyAfterInsert(GifEmoEntity entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }

    @Override
    protected Long getKey(GifEmoEntity entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    protected boolean isEntityUpdateable() {
        return true;
    }
}
