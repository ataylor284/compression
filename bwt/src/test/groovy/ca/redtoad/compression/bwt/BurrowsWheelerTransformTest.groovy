package ca.redtoad.compression.bwt

import spock.lang.Unroll
import spock.lang.Specification

class BurrowsWheelerTransformTest extends Specification {

    @Unroll
    def 'known string "#original" compresses and uncompresses back to the same string'() {
    given:
        def transformer = new BurrowsWheelerTransform()

    when:
        def transformed = new ByteArrayOutputStream()
        transformer.encode(new ByteArrayInputStream(original.bytes), transformed)
        def reversedStream = new ByteArrayOutputStream()
        transformer.decode(new ByteArrayInputStream(transformed.toByteArray()), reversedStream)
        def reversed = reversedStream.toString()

    then:
        reversed == original

    where:
        original                       | _
        '^BANANAx'                     | _
        ''                             | _
        'aaaaaaaaaaaaaaaaaaaaaaaaaaaa' | _
        'baaaaaaaaaaaaaaaaaaaaaaaaaaa' | _
        'test string for compression!' | _
        '¯\\_(ツ)_/¯'                  | _
    }
}
