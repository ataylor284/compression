package ca.redtoad.compression.mtf

import spock.lang.Unroll
import spock.lang.Specification

class MoveToFrontTransformTest extends Specification {

    @Unroll
    def 'known string "#original" compresses and uncompresses back to the same string'() {
    given:
        def transformer = new MoveToFrontTransform()

    when:
        println "test $original"
        def transformed = new ByteArrayOutputStream()
        transformer.encode(new ByteArrayInputStream(original.bytes), transformed)
        def reversedStream = new ByteArrayOutputStream()
        transformer.decode(new ByteArrayInputStream(transformed.toByteArray()), reversedStream)
        def reversed = reversedStream.toString()

    then:
        reversed == original

    where:
        original                       | _
        'BANANA'                       | _
        ''                             | _
        'aaaaaaaaaaaaaaaaaaaaaaaaaaaa' | _
        'baaaaaaaaaaaaaaaaaaaaaaaaaaa' | _
        'test string for compression!' | _
        '¯\\_(ツ)_/¯'                  | _
    }
}
