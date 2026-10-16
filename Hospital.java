 class hospital {
    void doctorStatus(String doctorName) {
        System.out.println("The name of doctor");
    }

    String doctorStatus(String doctorName, int age) {
        System.out.println("the doctor detail as follow,");
        return doctorName;
    }

    void fees() {
        System.out.println("No fees Structure");
    }
}

class minihospital extends hospital {
    void fees() {
        System.out.println("Here we apply fees pls pay fees");
    }

    void doctorStatus(String doctorName) {
        System.out.println("here doctor name is pawan");
    }
}

public class Hospital {
    public static void main(String[] ar) {
        hospital hs = new hospital();
        hs.doctorStatus("himanshu");
        hs.doctorStatus("himanshu", 12);

        minihospital ms = new minihospital();
        ms.fees();
        ms.doctorStatus("Himanshu");
    }
}

