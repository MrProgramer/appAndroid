package ir.pergar.app.data.db

import androidx.room.TypeConverter
import ir.pergar.app.data.model.AccountKind
import ir.pergar.app.data.model.DebtDirection
import ir.pergar.app.data.model.TxnSource
import ir.pergar.app.data.model.TxnType

class Converters {
    @TypeConverter fun txnTypeToString(value: TxnType): String = value.name
    @TypeConverter fun stringToTxnType(value: String): TxnType = TxnType.valueOf(value)

    @TypeConverter fun sourceToString(value: TxnSource): String = value.name
    @TypeConverter fun stringToSource(value: String): TxnSource = TxnSource.valueOf(value)

    @TypeConverter fun kindToString(value: AccountKind): String = value.name
    @TypeConverter fun stringToKind(value: String): AccountKind = AccountKind.valueOf(value)

    @TypeConverter fun directionToString(value: DebtDirection): String = value.name
    @TypeConverter fun stringToDirection(value: String): DebtDirection = DebtDirection.valueOf(value)
}
