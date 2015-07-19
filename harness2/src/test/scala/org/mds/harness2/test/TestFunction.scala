package org.mds.harness2.test

/**
 * Created by modoso on 15/4/25.
 */
object TestFunction {

    def go(f1: String => String)(f2: String => String) = {
        println(f1("1") + f2("2"))
    }

    def main(args: Array[String]) {
        go {
            case "1" => "a-----"
        } {
            case "2" => "b---------"
        }
    }
}
