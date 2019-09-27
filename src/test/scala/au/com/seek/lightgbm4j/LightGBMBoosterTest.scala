package au.com.seek.lightgbm4j

import org.scalatest.FreeSpec

class LightGBMBoosterTest extends FreeSpec {

  // this is the model from https://github.com/microsoft/LightGBM/tree/master/examples/binary_classification
  private val gbm = LightGBMBooster.fromGZResource("binary_classification_model.txt.gz")

  "should return same results as Python" in {

    // the first 3 lines of https://github.com/microsoft/LightGBM/blob/master/examples/binary_classification/binary.test (minus the label)
    val score = gbm.predictForMat(
      Array(
        Array(0.644, 0.247, -0.447, 0.862, 0.374, 0.854, -1.126, -0.790, 2.173, 1.015, -0.201, 1.400, 0.000, 1.575, 1.807, 1.607, 0.000, 1.585, -0.190, -0.744, 3.102, 0.958, 1.061, 0.980, 0.875, 0.581, 0.905, 0.796).map(_.toFloat),
        Array(0.385, 1.800, 1.037, 1.044, 0.349, 1.502, -0.966, 1.734, 0.000, 0.966, -1.960, -0.249, 0.000, 1.501, 0.465, -0.354, 2.548, 0.834, -0.440, 0.638, 3.102, 0.695, 0.909, 0.981, 0.803, 0.813, 1.149, 1.116).map(_.toFloat),
        Array(1.214, -0.166, 0.004, 0.505, 1.434, 0.628, -1.174, -1.230, 1.087, 0.579, -1.047, -0.118, 0.000, 0.835, 0.340, 1.234, 2.548, 0.711, -1.383, 1.355, 0.000, 0.848, 0.911, 1.043, 0.931, 1.058, 0.744, 0.696).map(_.toFloat)
      ))

    assert(score(0) == 0.66948276226373293)
    assert(score(1) == 0.35960455890544629)
    assert(score(2) == 0.12315245736311033)

  }

  "should predict the same results for a binary CSR matrix" in {

    val score1 = gbm.predictForMat(
      Array(
        Array(1, 0, 0, 0, 1, 1, 0, 0, 1, 1, 1),
        Array(1, 0, 0, 0, 1, 1, 0, 0, 1, 0, 1),
        Array(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0)
      ))

    val m = new BinaryCSRMatrix(
      Array(
        Array(0, 4, 5, 8, 9, 10),
        Array(0, 4, 5, 8, 10),
        Array(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
      ))

    val score2 = gbm.predictForCSR(m)

    assert(score1 == score2)

  }
}

