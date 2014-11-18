package com.rackspace.prefs

object env extends Function2[String,String,String] {
  def apply(name: String, default: String): String =
    if (System.getenv(name) != null) System.getenv(name) else default
  }

object envInt extends Function2[String,Int,Int] {
  def apply(name: String, default: Int): Int =
    if (System.getenv(name) != null) System.getenv(name).toInt else default
}

