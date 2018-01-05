package wmazur.fem

import com.google.gson.{Gson, GsonBuilder}
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import wmazur.utility.Range

class GlobalOptionsTest extends FlatSpec{
  "Constructor" should "return valid JSON" in {
  //  val options = new GlobalOptions(100.0, 1200.0, 500, 50, 300.0, 0.1, 0.1, 4, 4, 700.0, 25.0, 7800.0)
  }

  "Global options" should "should properly read from json" in {
    val options = new GlobalOptions("/home/wmazur/AGH_FEM/resources/globalOptions.json")
    val gson = new Gson
    val prettyGson = new GsonBuilder().setPrettyPrinting().create
    println(prettyGson.toJson(options))
    assert(gson.toJson(options).length > 5)
  }



}
