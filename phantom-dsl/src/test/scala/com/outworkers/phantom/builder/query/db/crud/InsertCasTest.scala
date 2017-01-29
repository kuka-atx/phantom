/*
 * Copyright 2013 - 2017 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.outworkers.phantom.builder.query.db.crud

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.builder.query.ExecutableStatementList
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables._
import com.outworkers.util.testing._

class InsertCasTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.primitives.insertSchema()
    TestDatabase.primitives.truncate().future().block(defaultScalaTimeout)
    TestDatabase.testTable.insertSchema()
    TestDatabase.recipes.insertSchema()
  }

  "Standard inserts" should "not create multiple database entries and perform upserts instead" in {
    val row = gen[Primitive]

    val insertion = new ExecutableStatementList(
      List(
        TestDatabase.primitives.store(row).ifNotExists().qb,
        TestDatabase.primitives.store(row).ifNotExists().qb,
        TestDatabase.primitives.store(row).ifNotExists().qb,
        TestDatabase.primitives.store(row).ifNotExists().qb,
        TestDatabase.primitives.store(row).ifNotExists().qb
      )
    )

    val chain = for {
      truncate <- TestDatabase.primitives.truncate.future()
      store <- insertion.future()
      one <- TestDatabase.primitives.select.where(_.pkey eqs row.pkey).one
      multi <- TestDatabase.primitives.select.where(_.pkey eqs row.pkey).fetch()
    } yield (one, multi)

    whenReady(chain) {
      case (res1, res3) => {
        info("The one query should return a record")
        res1 shouldBe defined

        info("And the record should equal the inserted record")
        res1.value shouldEqual row

        info("And only one record should be retrieved from a range fetch")
        res3 should have size 1
      }
    }
  }


  "Conditional inserts" should "not create duplicate database entries" in {
    val row = gen[Primitive]

    val insertion = new ExecutableStatementList(
      List(
        TestDatabase.primitives.store(row).ifNotExists().qb,
        TestDatabase.primitives.store(row).ifNotExists().qb,
        TestDatabase.primitives.store(row).ifNotExists().qb,
        TestDatabase.primitives.store(row).ifNotExists().qb,
        TestDatabase.primitives.store(row).ifNotExists().qb
      )
    )

    val chain = for {
      truncate <- TestDatabase.primitives.truncate.future()
      store <- insertion.future()
      one <- TestDatabase.primitives.select.where(_.pkey eqs row.pkey).one
      multi <- TestDatabase.primitives.select.where(_.pkey eqs row.pkey).fetch()
    } yield (one, multi)

    whenReady(chain) {
      case (res1, res3) => {
        info("The one query should return a record")
        res1 shouldBe defined

        info("And the record should equal the inserted record")
        res1.value shouldEqual row

        info("And only one record should be retrieved from a range fetch")
        res3 should have size 1
      }
    }
  }
}