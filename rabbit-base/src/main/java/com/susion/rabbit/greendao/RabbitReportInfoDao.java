package com.susion.rabbit.greendao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.susion.rabbit.entities.RabbitReportInfo;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "RABBIT_REPORT_INFO".
*/
public class RabbitReportInfoDao extends AbstractDao<RabbitReportInfo, Long> {

    public static final String TABLENAME = "RABBIT_REPORT_INFO";

    /**
     * Properties of entity RabbitReportInfo.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property InfoStr = new Property(1, String.class, "infoStr", false, "INFO_STR");
        public final static Property Time = new Property(2, Long.class, "time", false, "TIME");
        public final static Property DeviceInfoStr = new Property(3, String.class, "deviceInfoStr", false, "DEVICE_INFO_STR");
        public final static Property Type = new Property(4, String.class, "type", false, "TYPE");
        public final static Property UseTime = new Property(5, long.class, "useTime", false, "USE_TIME");
    }


    public RabbitReportInfoDao(DaoConfig config) {
        super(config);
    }
    
    public RabbitReportInfoDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"RABBIT_REPORT_INFO\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"INFO_STR\" TEXT," + // 1: infoStr
                "\"TIME\" INTEGER," + // 2: time
                "\"DEVICE_INFO_STR\" TEXT," + // 3: deviceInfoStr
                "\"TYPE\" TEXT," + // 4: type
                "\"USE_TIME\" INTEGER NOT NULL );"); // 5: useTime
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"RABBIT_REPORT_INFO\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, RabbitReportInfo entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String infoStr = entity.getInfoStr();
        if (infoStr != null) {
            stmt.bindString(2, infoStr);
        }
 
        Long time = entity.getTime();
        if (time != null) {
            stmt.bindLong(3, time);
        }
 
        String deviceInfoStr = entity.getDeviceInfoStr();
        if (deviceInfoStr != null) {
            stmt.bindString(4, deviceInfoStr);
        }
 
        String type = entity.getType();
        if (type != null) {
            stmt.bindString(5, type);
        }
        stmt.bindLong(6, entity.getUseTime());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, RabbitReportInfo entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String infoStr = entity.getInfoStr();
        if (infoStr != null) {
            stmt.bindString(2, infoStr);
        }
 
        Long time = entity.getTime();
        if (time != null) {
            stmt.bindLong(3, time);
        }
 
        String deviceInfoStr = entity.getDeviceInfoStr();
        if (deviceInfoStr != null) {
            stmt.bindString(4, deviceInfoStr);
        }
 
        String type = entity.getType();
        if (type != null) {
            stmt.bindString(5, type);
        }
        stmt.bindLong(6, entity.getUseTime());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public RabbitReportInfo readEntity(Cursor cursor, int offset) {
        RabbitReportInfo entity = new RabbitReportInfo( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // infoStr
            cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2), // time
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // deviceInfoStr
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // type
            cursor.getLong(offset + 5) // useTime
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, RabbitReportInfo entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setInfoStr(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setTime(cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2));
        entity.setDeviceInfoStr(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setType(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setUseTime(cursor.getLong(offset + 5));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(RabbitReportInfo entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(RabbitReportInfo entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(RabbitReportInfo entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
