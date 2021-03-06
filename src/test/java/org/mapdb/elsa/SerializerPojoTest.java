package org.mapdb.elsa;


import org.junit.Test;

import java.io.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class SerializerPojoTest{

    ElsaSerializerPojo p = new ElsaSerializerPojo();

    enum Order
    {
        ASCENDING,
        DESCENDING
    }
    private byte[] serialize(Object i) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream out2 = new DataOutputStream(out);
//        out2.writeObject(i);
        p.serialize(out2, i);
        return out.toByteArray();
    }

    private Object deserialize(byte[] buf) throws IOException {
        return p.deserialize(new DataInputStream(new ByteArrayInputStream(buf)));
    }


    @Test public void testEnum() throws Exception{
        Order o = Order.ASCENDING;
        o = ElsaSerializerBaseTest.clonePojo(o);
        assertEquals(o,Order.ASCENDING );
        assertEquals(o.ordinal(),Order.ASCENDING .ordinal());
        assertEquals(o.name(),Order.ASCENDING .name());

        o = Order.DESCENDING;
        o = ElsaSerializerBaseTest.clonePojo(o);
        assertEquals(o,Order.DESCENDING );
        assertEquals(o.ordinal(),Order.DESCENDING .ordinal());
        assertEquals(o.name(),Order.DESCENDING .name());

    }


    static class Extr  implements  Externalizable{

        public Extr(){}

        int aaa = 11;
        String  l = "agfa";

        @Override public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(l);
            out.writeInt(aaa);

        }

        @Override  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            l = (String) in.readObject();
            aaa = in.readInt()+1;

        }
    }

    @Test public void testExternalizable() throws Exception{
        Extr e = new Extr();
        e.aaa = 15;
        e.l = "pakla";

        e = (Extr) deserialize(serialize(e));
        assertEquals(e.aaa, 16); //was incremented during serialization
        assertEquals(e.l,"pakla");

    }


    static class Bean1 implements Serializable {

    	private static final long serialVersionUID = -2549023895082866523L;

		@Override
		public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Bean1 bean1 = (Bean1) o;

            if (Double.compare(bean1.doubleField, doubleField) != 0) return false;
            if (Float.compare(bean1.floatField, floatField) != 0) return false;
            if (intField != bean1.intField) return false;
            if (longField != bean1.longField) return false;
            if (field1 != null ? !field1.equals(bean1.field1) : bean1.field1 != null) return false;
            if (field2 != null ? !field2.equals(bean1.field2) : bean1.field2 != null) return false;

            return true;
        }


        protected String field1 = null;
        protected String field2 = null;

        protected int intField = Integer.MAX_VALUE;
        protected long longField = Long.MAX_VALUE;
        protected double doubleField = Double.MAX_VALUE;
        protected float floatField = Float.MAX_VALUE;

        transient int getCalled = 0;
        transient int setCalled = 0;

        public String getField2() {
            getCalled++;
            return field2;
        }

        public void setField2(String field2) {
            setCalled++;
            this.field2 = field2;
        }

        Bean1(String field1, String field2) {
            this.field1 = field1;
            this.field2 = field2;
        }

        Bean1() {
        }
    }

    static class Bean2 extends Bean1 {

		private static final long serialVersionUID = 8376654194053933530L;

		@Override
		public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            Bean2 bean2 = (Bean2) o;

            if (field3 != null ? !field3.equals(bean2.field3) : bean2.field3 != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return field3 != null ? field3.hashCode() : 0;
        }

        private String field3 = null;

        Bean2(String field1, String field2, String field3) {
            super(field1, field2);
            this.field3 = field3;
        }

        Bean2() {
        }
    }



    Bean1 b = new Bean1("aa", "bb");
    Bean2 b2 = new Bean2("aa", "bb", "cc");

    @Test public void testGetFieldValue1() throws Exception {
        assertEquals("aa", p.getFieldValue(new ElsaSerializerPojo.FieldInfo("field1",String.class.getName(),String.class,b.getClass()), b));
    }

    @Test public void testGetFieldValue2() throws Exception {
        assertEquals("bb", p.getFieldValue(new ElsaSerializerPojo.FieldInfo("field2",String.class.getName(),String.class,b.getClass()), b));
        assertEquals(0, b.getCalled);
    }

    @Test public void testGetFieldValue3() throws Exception {
        assertEquals("aa", p.getFieldValue(new ElsaSerializerPojo.FieldInfo("field1",String.class.getName(),String.class,b2.getClass()), b2));
    }

    @Test public void testGetFieldValue4() throws Exception {
        assertEquals("bb", p.getFieldValue(new ElsaSerializerPojo.FieldInfo("field2",String.class.getName(),String.class,b2.getClass()), b2));
        assertEquals(0, b2.getCalled);
    }

    @Test public void testGetFieldValue5() throws Exception {
        assertEquals("cc", p.getFieldValue(new ElsaSerializerPojo.FieldInfo("field3",String.class.getName(),String.class,b2.getClass()), b2));
    }



    @Test public void testSerializable() throws Exception {

        assertEquals(b, ElsaSerializerBaseTest.clonePojo(b));
    }


    @Test public void testRecursion() throws Exception {
        AbstractMap.SimpleEntry b = new AbstractMap.SimpleEntry("abcd", null);
        b.setValue(b.getKey());

        AbstractMap.SimpleEntry bx = ElsaSerializerBaseTest.clonePojo(b);
        assertEquals(bx, b);
        assert (bx.getKey() == bx.getValue());

    }

    @Test public void testRecursion2() throws Exception {
        AbstractMap.SimpleEntry b = new AbstractMap.SimpleEntry("abcd", null);
        b.setValue(b);

        AbstractMap.SimpleEntry bx = ElsaSerializerBaseTest.clonePojo(b);
        assertTrue(bx == bx.getValue());
        assertEquals(bx.getKey(), "abcd");

    }


    @Test public void testRecursion3() throws Exception {
        ArrayList l = new ArrayList();
        l.add("123");
        l.add(l);

        ArrayList l2 = ElsaSerializerBaseTest.clonePojo(l);

        assertTrue(l2.size() == 2);
        assertEquals(l2.get(0), "123");
        assertTrue(l2.get(1) == l2);
    }

    public static class test_transient implements Serializable{
        transient int aa = 11;
        transient String ss = "aa";
        int bb = 11;
    }

    @Test public void test_transient() throws IOException {
        test_transient t = new test_transient();
        t.aa = 12;
        t.ss = "bb";
        t.bb = 13;
        t = (test_transient) ElsaSerializerBaseTest.clonePojo(t);
        assertEquals(0,t.aa);
        assertEquals(null,t.ss);
        assertEquals(13,t.bb);
    }

    @Test public void test_transient2(){
        test_transient t = new test_transient();
        t.aa = 12;
        t.ss = "bb";
        t.bb = 13;

        t = outputStreamClone(t);
        assertEquals(0,t.aa);
        assertEquals(null,t.ss);
        assertEquals(13,t.bb);
                }

                /* clone value using serialization */
                public static <E> E outputStreamClone(E value){
                    try{
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        new ObjectOutputStream(out).writeObject(value);
                        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()));
                        return (E) in.readObject();
                    }catch(Exception ee){
                        throw new IOError(ee);
                    }
                }

                //this can not be serialized, it alwaes throws exception on serialization
                static final class RealClass implements Serializable, Externalizable{
                    @Override
                    public void writeExternal(ObjectOutput out) throws IOException {
                        throw new Error();
                    }

                    @Override
                    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
                        throw new Error();
                    }
                }

                //this is placeholder which gets serialized instead
                static final class PlaceHolder implements Serializable{

                }

                public static class MM extends AbstractMap implements Serializable{

                    Map m = new HashMap();

        private Object writeReplace() throws ObjectStreamException {
            return new LinkedHashMap(this);
        }

        @Override
        public Set<Entry> entrySet() {
            return m.entrySet();
        }

        @Override
        public Object put(Object key, Object value) {
            return m.put(key,value);
        }
    }

    @Test
    public void testWriteReplace() throws IOException {
        Map m = new MM();
        m.put("11","111");
        assertEquals(new LinkedHashMap(m), ElsaSerializerBaseTest.clonePojo(m));
    }


    @Test
    public void testWriteReplaceWrap() throws IOException {
        Map m = new MM();
        m.put("11","111");
        assertEquals(new LinkedHashMap(m), ElsaSerializerBaseTest.clonePojo(m));
    }

    static class WriteReplaceAA implements Serializable{
        Object writeReplace() throws ObjectStreamException {
            return "";
        }

    }

    static class WriteReplaceBB implements Serializable{
        WriteReplaceAA aa = new WriteReplaceAA();
    }



    @Test(expected = ClassCastException.class)
    public void java_serialization_writeReplace_in_object_graph() throws IOException, ClassNotFoundException {
        ElsaSerializerBaseTest.cloneJava(new WriteReplaceBB());
    }

    static  class ExtHashMap extends HashMap<String,String>{}



    @Test public void java_serialization(){
        assertTrue(ElsaSerializerPojo.useJavaSerialization(ExtHashMap.class));
    }

    Class lastMissingClass;
    ElsaClassCallback lastMissingClassCallback = new ElsaClassCallback() {
        @Override
        public void classMissing(Class clazz) {
            lastMissingClass = clazz;
        }
    };

    @Test public void unknown_class_notified() throws IOException {
        Object bean = new Serialization2Bean();
        ElsaSerializerPojo p = new ElsaSerializerPojo(null, 0, null, null, null, null, lastMissingClassCallback, null);
        p.serialize(new DataOutputStream(new ByteArrayOutputStream()), bean);
        assertEquals(lastMissingClass, bean.getClass());
    }

    @Test public void known_class_not_notified() throws IOException {
        Object bean = new Serialization2Bean();
        ElsaSerializerPojo p = new ElsaSerializerPojo(null, 0, null, null, null, null,  lastMissingClassCallback,
                new ElsaClassInfoResolver.ArrayBased(new Class[]{bean.getClass()}, null)
                );
        p.serialize(new DataOutputStream(new ByteArrayOutputStream()), bean);
        assertEquals(lastMissingClass, null);
    }

    public static class WithClassField implements Serializable {
        int someValue = 1;
        Class someClass;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            WithClassField that = (WithClassField) o;

            if (someValue != that.someValue) return false;
            return someClass != null ? someClass.equals(that.someClass) : that.someClass == null;

        }

        @Override
        public int hashCode() {
            int result = someValue;
            result = 31 * result + (someClass != null ? someClass.hashCode() : 0);
            return result;
        }
    }

    @Test public void class_with_class_field() throws IOException {
        WithClassField w = new WithClassField();
        w.someClass = String.class;

        assertEquals(w, outputStreamClone(w));
        assertEquals(w, ElsaSerializerBaseTest.clonePojo(w, p));
    }


    @Test public void unresolved_class_stored_in_stream() throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bout);

        p.serialize(out, new IntBean(5));

        DataInput in = new DataInputStream(new ByteArrayInputStream(bout.toByteArray()));

        assertEquals(ElsaSerializerBase.Header.POJO_CLASSINFO, in.readUnsignedByte());
        assertEquals(0, ElsaUtil.unpackInt(in));
        assertEquals(p.makeClassInfo(IntBean.class, null), p.classInfoDeserialize(in));

        assertEquals(ElsaSerializerBase.Header.POJO, in.readUnsignedByte());
        assertEquals(0, ElsaUtil.unpackInt(in)); //class id
        assertEquals(1, ElsaUtil.unpackInt(in)); //number of fields
        assertEquals(0, ElsaUtil.unpackInt(in)); //field id
        assertEquals(ElsaSerializerBase.Header.INT_5, in.readUnsignedByte()); //field value

        assertEquals(-1, ((InputStream)in).read());
    }

    @Test public void classInfoClone() throws IOException {
        ElsaSerializerPojo.ClassInfo c = p.makeClassInfo(IntBean.class, null);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bout);
        p.classInfoSerialize(out, c);

        DataInput in = new DataInputStream(new ByteArrayInputStream(bout.toByteArray()));
        ElsaSerializerPojo.ClassInfo c2 = p.classInfoDeserialize(in);

        assertEquals(c, c2);
    }

}
