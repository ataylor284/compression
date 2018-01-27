package ca.redtoad.bitstream

import spock.lang.Unroll
import spock.lang.Specification

class BitOutputStreamTest extends Specification {

    @Unroll
    def "parameters #values write the expected values"() {
    given:
        def out = new ByteArrayOutputStream()
        def bitout = new BitOutputStream(out)

    when:
        for (v in values) {
            bitout.write(v[0], v[1])
        }
        bitout.flush()
        def result = out.toByteArray().collect { Byte.toUnsignedInt(it) }

    then:
        result == expectedResult

    where:
        values                     || expectedResult
        [[1, 8]]                   || [1]
        [[0xFF, 8]]                || [255]
        [[-1, 8]]                  || [255]
        [[0xF, 4]]                 || [15]
        [[0x0, 4], [0xF, 4]]       || [240]
        [[0x0, 1], [0xFF, 8]]      || [254, 1]
        [[-1, 4], [-1, 8]]         || [255, 15]
        [[0, 1], [-1, 8], [0, 1]]  || [254, 1]
        [[511, 9]]                 || [255, 1]
        [[65535, 16], [65535, 16]] || [255, 255, 255, 255]
        [[1, 9], [1, 9], [1, 9]]   || [1, 2, 4, 0]
    }
}
