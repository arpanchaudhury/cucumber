/*
 * Copyright (c) 2017, Michael Lewis
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.waioeka.sbt.runner

import cucumber.runtime.io.{MultiLoader, ResourceLoaderClassFinder}
import cucumber.runtime.{Runtime, RuntimeOptions}
import sbt.testing._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Try


/** Cucumber test runner. */
case class CucumberRunner(args: Array[String],
                          remoteArgs: Array[String],
                          testClassLoader: ClassLoader)
  extends Runner {

  def runTest(selectors: Seq[String],
              loggers: Seq[Logger],
              name: String,
              eventHandler: EventHandler) = {


    def info(s: String) = loggers foreach (_ info s)

    def handle(op: OptionalThrowable, st: Status) = {
      eventHandler.handle(new Event {
        def fullyQualifiedName() = name
        def throwable() = op
        def status() = st
        def selector() = new TestSelector(fullyQualifiedName())
        def fingerprint() = new SubclassFingerprint {
          def superclassName = classOf[CucumberSpec].getName
          def isModule = false
          def requireNoArgConstructor = false
        }
        def duration() = 0
      })
    }

    info(s"suite: $name .")

    /* default cucumber arguments. */
    val cArgs = List("--glue","") ::: List("--plugin", "pretty") :::
                List("--plugin", "html:html") :::
                List("--plugin", "json:json") ::: List("classpath:")

      /* at present, run everything serially. */
      val result = invokeCucumber(cArgs,testClassLoader).recover {
        case t: Throwable => handle(new OptionalThrowable(t), Status.Failure)
      }.get

      result match {
        case 0 =>
            info(s"test: $name  ... success")
            handle(new OptionalThrowable(), Status.Success)
        case 1 =>
            info(s"test: $name ... failed")
            handle(new OptionalThrowable(), Status.Failure)
      }
    }


  /** Runs the actual cucumber arguments. */
  def invokeCucumber(args: List[String], cl: ClassLoader) = Try {
    import scala.collection.JavaConverters._
    val opts = new RuntimeOptions(args.asJava)
    val rl = new MultiLoader(cl)
    val cf = new ResourceLoaderClassFinder(rl,cl)
    val runtime = new Runtime(rl, cf, cl, opts)
    runtime.run()
    runtime.printSummary()
    runtime.exitStatus()
  }

  /** Output summary details. */
  override def done(): String = {
    s"done:"
  }

  override def tasks(taskDefs: Array[TaskDef]): Array[Task] = taskDefs.map(createTask)

  /** Create task from `TaskDef` */
  def createTask(t: TaskDef): Task = {
    new Task {
      override def taskDef(): TaskDef = t
      override def execute(eventHandler: EventHandler, loggers: Array[Logger]): Array[Task] = {
        runTest(Seq.empty,loggers,t.fullyQualifiedName(),eventHandler)
        Array.empty
      }
      override def tags(): Array[String] = Array.empty
    }
  }

}
