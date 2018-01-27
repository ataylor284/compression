package ca.redtoad.bitstream

import spock.lang.Unroll
import spock.lang.Specification

class BitInputStreamTest extends Specification {

    @Unroll
    def 'foo #inputbytes #bitsToRead'() {
    when:
        def input = new ByteArrayInputStream(inputbytes as byte[])
        def bitinput = new BitInputStream(input)

        def result = bitsToRead.collect { bitinput.read(it) }

    then:
        result == expectedResult

    where:
        inputbytes   | bitsToRead || expectedResult
        [1]          | [8, 8]     || [1, -1]
        [0xA5]       | [4, 4]     || [5, 10]
        [0xFF]       | [1, 7]     || [1, 127]
        [0xAA, 0x55] | [6, 6]     || [42, 22]
        [0x5A, 0x1]  | [1, 1, 8]  || [0, 1, 86]
        [0xFF, 0x1]  | [9]        || [511]
        [0xFF, 0xFF] | [9]        || [511]
        [0xFF, 0xFF] | [16]       || [65535]
    }
}
