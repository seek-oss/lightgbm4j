package au.com.seek.lightgbm4j

import java.io.{File, FileOutputStream, InputStream}
import java.util.zip.GZIPInputStream

import com.microsoft.ml.lightgbm._
import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.reflect.FieldUtils

import scala.collection.mutable.ArrayBuffer

class LightGBMBooster(private val modelFile: String) extends Serializable with LazyLogging {

  private val boosterPtr: SWIGTYPE_p_void = getModel()

  def predictForMat(matrix: Array[Array[Float]]): IndexedSeq[Double] = {
    scoped { implicit s =>
      val numRows = matrix.length
      val numCols = matrix.head.length

      val data = generateData(numRows, matrix)

      val numRows_int32_tPtr          = toInt32Ptr(numRows)
      val numCols_int32_tPtr          = toInt32Ptr(numCols)
      val scoredDataLength_int64_tPtr = toInt64Ptr(numRows)

      val isRowMajor = 1

      val scoredDataOutPtr = s.new_doubleArray(numRows)

      val datasetParams = ""
      validate(
        lightgbmlib.LGBM_BoosterPredictForMat(
          boosterPtr,
          data,
          lightgbmlibConstants.C_API_DTYPE_FLOAT32,
          numRows_int32_tPtr,
          numCols_int32_tPtr,
          isRowMajor,
          lightgbmlibConstants.C_API_PREDICT_NORMAL,
          -1,
          datasetParams,
          scoredDataLength_int64_tPtr,
          scoredDataOutPtr
        ),
        "BoosterPredictForMat"
      )

      val results = Array.ofDim[Double](numRows)
      for (i <- results.indices) {
        results(i) = lightgbmlib.doubleArray_getitem(scoredDataOutPtr, i)
      }

      results
    }
  }

  def predictForCSR(matrix: BinaryCSRMatrix): IndexedSeq[Double] =
    predictForCSR(0, matrix.indPtr, matrix.indices, matrix.data)

  private def getModel(): SWIGTYPE_p_void = {
    logger.info(s"Constructing LightGBM booster from model: $modelFile")

    scoped { implicit s =>
      val boosterOutPtr = s.voidpp_handle()
      val numItersOut   = s.new_intp()
      validate(lightgbmlib.LGBM_BoosterCreateFromModelfile(modelFile, numItersOut, boosterOutPtr), "BoosterCreateFromModelfile")

      lightgbmlib.voidpp_value(boosterOutPtr)
    }
  }

  private def generateData(numRows: Int, matrix: Array[Array[Float]])(implicit s: SWIG_Allocations_Scope): SWIGTYPE_p_void = {
    val numCols = matrix.head.length
    val data    = s.new_floatArray(numCols * numRows)

    for (r <- matrix.indices) {
      val row = matrix(r)
      for (c <- row.indices) {
        lightgbmlib.floatArray_setitem(data, c + (r * numCols), row(c))
      }
    }

    lightgbmlib.float_to_voidp_ptr(data)
  }

  private def predictForCSR(numCols: Int, indPtr: Array[Int], indices: Array[Int], data: Array[Float]): IndexedSeq[Double] = {
    assert(indPtr.length > 1)
    assert(indices.length == data.length)
    assert(!indices.exists(i => i < 0 || (numCols != 0 && i >= numCols)))

    scoped { implicit s =>
      val numRows = indPtr.length - 1

      val indPtrPtr  = newIntArray(indPtr)
      val indicesPtr = newIntArray(indices)
      val dataPtr    = newFloatArray(data)

      val numIndPtr_int64_tPtr = toInt64Ptr(indPtr.length)
      val numElem_int64_tPtr   = toInt64Ptr(data.length)
      val numCols_int64_tPtr   = toInt64Ptr(numCols)

      val nPreds                     = numRows
      val predsOutPtr                = s.new_doubleArray(nPreds)
      val predsDataLength_int64_tPtr = toInt64Ptr(nPreds)

      val datasetParams = ""
      validate(
        lightgbmlib.LGBM_BoosterPredictForCSR(
          boosterPtr,
          toSWIGTYPE_p_void(indPtrPtr),
          lightgbmlibConstants.C_API_DTYPE_INT32,
          indicesPtr,
          dataPtr,
          lightgbmlibConstants.C_API_DTYPE_FLOAT32,
          numIndPtr_int64_tPtr,
          numElem_int64_tPtr,
          numCols_int64_tPtr,
          lightgbmlibConstants.C_API_PREDICT_NORMAL,
          -1,
          datasetParams,
          predsDataLength_int64_tPtr,
          predsOutPtr
        ),
        "BoosterPredictForCSR"
      )

      val results = Array.ofDim[Double](nPreds)
      for (i <- results.indices) {
        results(i) = lightgbmlib.doubleArray_getitem(predsOutPtr, i)
      }

      results
    }
  }

  private def validate(result: Int, component: String): Unit = {
    if (result == -1) {
      throw new UnsupportedOperationException(s"$component call failed in LightGBM with error: ${lightgbmlib.LGBM_GetLastError()}")
    }
  }

  private def newIntArray(array: Array[Int])(implicit s: SWIG_Allocations_Scope): SWIGTYPE_p_int32_t = {
    val data = s.new_intArray(array.length)
    for (i <- array.indices) {
      lightgbmlib.intArray_setitem(data, i, array(i))
    }
    lightgbmlib.int_to_int32_t_ptr(data)
  }

  private def newFloatArray(array: Array[Float])(implicit s: SWIG_Allocations_Scope): SWIGTYPE_p_void = {
    val data = s.new_floatArray(array.length)
    for (i <- array.indices) {
      lightgbmlib.floatArray_setitem(data, i, array(i))
    }
    lightgbmlib.float_to_voidp_ptr(data)
  }

  private def toInt64Ptr(value: Int)(implicit s: SWIG_Allocations_Scope): SWIGTYPE_p_int64_t = {
    val longPtr = s.new_longp()
    lightgbmlib.longp_assign(longPtr, value)
    lightgbmlib.long_to_int64_t_ptr(longPtr)
  }

  private def toInt32Ptr(value: Int)(implicit s: SWIG_Allocations_Scope): SWIGTYPE_p_int32_t = {
    val intP = s.new_intp()
    lightgbmlib.intp_assign(intP, value)
    lightgbmlib.int_to_int32_t_ptr(intP)
  }

  override def finalize(): Unit =
    lightgbmlib.LGBM_BoosterFree(boosterPtr)

  private def scoped[T](body: SWIG_Allocations_Scope => T): T = {
    val scope = new SWIG_Allocations_Scope
    try {
      body(scope)
    } finally {
      scope.finished()
    }
  }

  private class SWIG_Allocations_Scope {

    private val frees = ArrayBuffer.empty[() => Unit]

    def new_intArray(size: Int): SWIGTYPE_p_int = {
      val p = lightgbmlib.new_intArray(size)
      frees.append(() => lightgbmlib.delete_intArray(p))
      p
    }

    def new_doubleArray(size: Int): SWIGTYPE_p_double = {
      val p = lightgbmlib.new_doubleArray(size)
      frees.append(() => lightgbmlib.delete_doubleArray(p))
      p
    }

    def new_floatArray(size: Int): SWIGTYPE_p_float = {
      val p = lightgbmlib.new_floatArray(size)
      frees.append(() => lightgbmlib.delete_floatArray(p))
      p
    }

    def new_intp(): SWIGTYPE_p_int = {
      val p = lightgbmlib.new_intp()
      frees.append(() => lightgbmlib.delete_intp(p))
      p
    }

    def voidpp_handle(): SWIGTYPE_p_p_void = {
      val p = lightgbmlib.voidpp_handle()
      frees.append(() => lightgbmlib.delete_voidpp(p))
      p
    }

    def new_longp(): SWIGTYPE_p_long = {
      val p = lightgbmlib.new_longp()
      frees.append(() => lightgbmlib.delete_longp(p))
      p
    }

    def finished() =
      frees.foreach(_())
  }

  def toSWIGTYPE_p_void(in: SWIGTYPE_p_int32_t): SWIGTYPE_p_void = {
    val f = FieldUtils.getField(in.getClass, "swigCPtr", true)
    val p = FieldUtils.readField(f, in)

    new _SWIGTYPE_p_void(p.asInstanceOf[java.lang.Long].longValue())
  }

  class _SWIGTYPE_p_void(ptr: Long) extends SWIGTYPE_p_void(ptr, true)

}

object LightGBMBooster extends LazyLogging {

  private var loaded = false

  {
    loadLibraries()
  }

  def fromFile(modelFile: String): LightGBMBooster =
    new LightGBMBooster(modelFile)

  def fromGZResource(fromResource: String): LightGBMBooster =
    new LightGBMBooster(extractGZModel(fromResource))

  def loadLibraries(): Unit = {
    if (!loaded) {
      val os = System.getProperty("os.name")

      val (dir, ext) =
        if ("Mac OS X" == os)
        // Use the local version housed in the resource dir
          ("lib_lightgbm/mac_osx/", "dylib")
        else
        // Use the version shipped with the distribution
          ("lib_lightgbm/x86_64/", "so")

      loadLib(dir, "lib_lightgbm." + ext)
      loadLib(dir, "lib_lightgbm_swig." + ext)

      loaded = true
    }
  }

  protected def extractGZModel(fromResource: String): String = {
    logger.info(s"Extracting LightGBM model: $fromResource")

    val from = new GZIPInputStream(getClass.getClassLoader.getResourceAsStream(fromResource))
    try {
      extractToTmp(from, "lightgbm.model").getAbsolutePath
    } finally {
      from.close()
    }
  }

  private def loadLib(fromResourceDir: String, name: String): Unit = {
    logger.info(s"Loading LightGBM JNI library: $name")

    val from = getClass.getClassLoader.getResourceAsStream(fromResourceDir + name)
    try {
      System.load(extractToTmp(from, name).getAbsolutePath)
    } finally {
      from.close()
    }
  }

  private def extractToTmp(resource: InputStream, outputName: String): File = {
    val prefix = StringUtils.substringBeforeLast(outputName, ".")
    val suffix = StringUtils.substringAfterLast(outputName, ".")

    val outfile = File.createTempFile(prefix, suffix)
    outfile.deleteOnExit()

    val out = new FileOutputStream(outfile)
    try {
      IOUtils.copy(resource, out, 1024 * 1024)
    } finally {
      out.close()
    }

    outfile
  }
}
