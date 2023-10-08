package routing.repository

import routing.model.{Building, Crossroad, Street}
import zio.schema.DeriveSchema
import zio.sql.postgresql.PostgresJdbcModule

trait BuildingTableDescription extends PostgresJdbcModule {
  implicit val buildingSchema = DeriveSchema.gen[Building]

  val building = defineTable[Building]
  val (id, longitude, latitude, name) = building.columns
}

trait CrossroadTableDescription extends  PostgresJdbcModule {
  implicit val crossroadSchema = DeriveSchema.gen[Crossroad]

  val crossroad = defineTable[Crossroad]
  val (id, longitude, latitude) = crossroad.columns
}

trait StreetTableDescription extends  PostgresJdbcModule {
  implicit val streetSchema = DeriveSchema.gen[Street]

  val street = defineTable[Street]
  val (fromId, toId, name) = street.columns
}
