package ca.redtoad.compression.huffman

import spock.lang.Specification

import ca.redtoad.bitstream.BitInputStream
import ca.redtoad.bitstream.BitOutputStream

class HuffmanNodeTest extends Specification {

    def 'deserialize creates expected tree'() {
    when:
        def encoded = new ByteArrayOutputStream()
        def bitout = new BitOutputStream(encoded)
        bitout.write(1, 1) // non-leaf
        bitout.write(0, 1) // leaf
        bitout.write(97, 8) // 'a'
        bitout.write(0, 1) // leaf
        bitout.write(98, 8) // 'b'
        bitout.flush()
        def tree = HuffmanNode.deserialize(new BitInputStream(new ByteArrayInputStream(encoded.toByteArray())))
        tree.print(0)

    then:
        tree.isLeaf() == false
        tree.freq == 2
        tree.values.contains('a'.bytes[0])
        tree.values.contains('b'.bytes[0])
        tree.left.isLeaf() == true
        tree.left.freq == 1
        tree.right.isLeaf() == true
        tree.right.freq == 1
    }

    def 'serialize creates expected string'() {
    when:
        def tree = HuffmanNode.merge(new HuffmanNode(1, [Byte.valueOf('a'.bytes[0])] as Set, null, null), new HuffmanNode(1, [Byte.valueOf('b'.bytes[0])] as Set, null, null))
        def output = new ByteArrayOutputStream()
        def bitout = new BitOutputStream(output)
        tree.serialize(bitout)
        bitout.flush()

    then:
        def encoded = new ByteArrayInputStream(output.toByteArray())
        def bitin = new BitInputStream(encoded)

        bitin.read(1) == 1 // non-leaf
        bitin.read(1) == 0 // leaf
        bitin.read(8) == 97 // 'a'
        bitin.read(1) == 0 // leaf
        bitin.read(8) == 98 // 'a'
    }
}
