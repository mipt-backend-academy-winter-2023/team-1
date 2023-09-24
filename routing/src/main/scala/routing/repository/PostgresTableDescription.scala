package routing.repository

import routing.model.{Building, Crossroad, Street}
import zio.schema.DeriveSchema
import zio.sql.postgresql.PostgresJdbcModule

trait PostgresTableDescription extends PostgresJdbcModule {
  implicit val buildingSchema = DeriveSchema.gen[Building]
  implicit val crossroadSchema = DeriveSchema.gen[Crossroad]
  implicit val streetSchema = DeriveSchema.gen[Street]

  val building = defineTable[Building]
  val crossroad = defineTable[Crossroad]
  val street = defineTable[Street]

  val (id, longitude, latitude, name) = building.columns
  val (id, longitude, latitude) = crossroad.columns
  val (fromId, toId, name) = street.columns
}