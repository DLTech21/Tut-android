package com.dtalk.dd.DB.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.dtalk.dd.DB.entity.ApplicantEntity;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

/**
 * Created by Donal on 16/4/27.
 */
public class ApplicantDao extends AbstractDao<ApplicantEntity, Long> {

    public static final String TABLENAME = "Applicant";

    /**
     * Properties of entity DepartmentEntity.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", false, "_id");
        public final static Property Uid = new Property(1, int.class, "uid", true, "UID");
        public final static Property Avatar = new Property(2, String.class, "avatar", false, "AVATAR");
        public final static Property Nickname = new Property(3, String.class, "nickname", false, "NICKNAME");
        public final static Property Msg = new Property(4, String.class, "msg", false, "MSG");
        public final static Property Type = new Property(5, int.class, "type", false, "TYPE");
        public final static Property Response = new Property(6, int.class, "response", false, "RESPONSE");
    };


    public ApplicantDao(DaoConfig config) {
        super(config);
    }

    public ApplicantDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'Applicant' (" + //
                "'_id' INTEGER NOT NULL ," + // 0: id
                "'UID' INTEGER NOT NULL PRIMARY KEY ," + // 1: departId
                "'AVATAR' TEXT NOT NULL ," + // 2: AVATAR
                "'NICKNAME' TEXT NOT NULL ," + // 3: NICKNAME
                "'MSG' TEXT NOT NULL ," + // 4: MSG
                "'TYPE' INTEGER NOT NULL ," + // 5: TYPE
                "'RESPONSE' INTEGER NOT NULL );"); // 6: RESPONSE
        // Add Indexes
        db.execSQL("CREATE INDEX " + constraint + "IDX_Applicant_UID ON Applicant" +
                " (UID);");
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'Applicant'";
        db.execSQL(sql);
    }

    @Override
    protected ApplicantEntity readEntity(Cursor cursor, int offset) {
        ApplicantEntity entity = new ApplicantEntity( //
                cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
                cursor.getInt(offset + 1), // uid
                cursor.getString(offset + 2), // avatar
                cursor.getString(offset + 3), // nickname
                cursor.getString(offset + 4), // msg
                cursor.getInt(offset + 5), // type
                cursor.getInt(offset + 6) // response
        );
        return entity;
    }

    @Override
    protected Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }

    @Override
    protected void readEntity(Cursor cursor, ApplicantEntity entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setUid(cursor.getInt(offset + 1));
        entity.setAvatar(cursor.getString(offset + 2));
        entity.setNickname(cursor.getString(offset + 3));
        entity.setMsg(cursor.getString(offset + 4));
        entity.setType(cursor.getInt(offset + 5));
        entity.setResponse(cursor.getInt(offset + 6));
    }

    @Override
    protected void bindValues(SQLiteStatement stmt, ApplicantEntity entity) {
        stmt.clearBindings();

        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getUid());
        stmt.bindString(3, entity.getAvatar());
        stmt.bindString(4, entity.getNickname());
        stmt.bindString(5, entity.getMsg());
        stmt.bindLong(6, entity.getType());
        stmt.bindLong(7, entity.getResponse());
    }

    @Override
    protected Long updateKeyAfterInsert(ApplicantEntity entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }

    @Override
    protected Long getKey(ApplicantEntity entity) {
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
