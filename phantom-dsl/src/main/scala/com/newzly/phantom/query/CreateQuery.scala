package com.newzly.phantom.query

import scala.concurrent.{ Future => ScalaFuture }
import com.datastax.driver.core.{ ResultSet, Session }
import com.newzly.phantom.{ CassandraResultSetOperations, CassandraTable }
import com.twitter.util.Future

class CreateQuery[T <: CassandraTable[T, R], R](table: T, query: String) extends CassandraResultSetOperations {
  def apply(): CreateQuery[T, R] = {
    val queryInit = s"CREATE TABLE ${table.tableName} ("
    val queryColumns = table.columns.foldLeft("")((qb, c) => {
      s"$qb, ${c.name} ${c.cassandraType}"
    })

    val pkes = table.primaryKeys.map(_.name).mkString(",")
    table.logger.info(s"Adding Primary keys indexes: $pkes")
    val queryPrimaryKey  = if (pkes.length > 0) s", PRIMARY KEY ($pkes)" else ""
    new CreateQuery(table, queryInit + queryColumns.drop(1) + queryPrimaryKey + ")")
  }

  def queryString: String = {
    if (query.last != ';') query + ";" else query
  }

  def execute()(implicit session: Session): Future[ResultSet] =  {
    queryStringExecuteToFuture(queryString)
  }

  def future()(implicit session: Session): ScalaFuture[ResultSet] = {
    scalaQueryStringExecuteToFuture(queryString)
  }
}