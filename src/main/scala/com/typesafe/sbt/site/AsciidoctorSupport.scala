package com.typesafe.sbt.site
package site

import org.asciidoctor.Asciidoctor.Factory
import org.asciidoctor.{AsciiDocDirectoryWalker, Options, SafeMode}
import sbt.Keys._
import sbt._

object AsciidoctorSupport {

  val Asciidoctor = config("asciidoctor")

  val settings: Seq[Setting[_]] =
    Seq(
      sourceDirectory in Asciidoctor <<= sourceDirectory(_ / "asciidoctor"),
      target in Asciidoctor <<= target(_ / "asciidoctor"),
      includeFilter in Asciidoctor := AllPassFilter) ++ inConfig(Asciidoctor)(Seq(
      mappings <<= (sourceDirectory, target, includeFilter) map AsciidoctorRunner.run))
}

object AsciidoctorRunner {

  def run(input: File, output: File, includeFilter: FileFilter): Seq[(File, String)] = {
    val oldContextClassLoader = Thread.currentThread().getContextClassLoader
    Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader())
    val asciidoctor = Factory.create()
    if (!output.exists) output.mkdirs()
    val options = new Options
    options.setToDir(output.getAbsolutePath())
    options.setDestinationDir(output.getAbsolutePath())
    options.setSafe(SafeMode.UNSAFE)
    asciidoctor.renderDirectory(new AsciiDocDirectoryWalker(input.getAbsolutePath()), options)
    val inputImages = input / "images"
    if (inputImages.exists()) {
      val outputImages = output / "images"
      IO.copyDirectory(inputImages, outputImages, true)
    }
    Thread.currentThread().setContextClassLoader(oldContextClassLoader);
    output ** includeFilter --- output x relativeTo(output)
  }

}
