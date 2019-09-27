package au.com.seek.lightgbm4j

class BinaryCSRMatrix(colIndices: Array[Array[Int]]) {

  val numRows: Int = colIndices.length

  val indices: Array[Int] = colIndices.flatten
  assert(!indices.exists(i => i < 0))

  def numCols: Int = indices.max + 1

  val indPtr: Array[Int] = colIndices.foldLeft(Array(0))((a, row) => a :+ a.last + row.length)

  def data: Array[Float] = Array.fill[Float](indices.length)(1f)

}
