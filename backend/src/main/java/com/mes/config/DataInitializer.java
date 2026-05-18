package com.mes.config;

import com.mes.entity.*;
import com.mes.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired private RoleRepository roleRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private ProductRepository productRepo;
    @Autowired private ProcessStepRepository stepRepo;
    @Autowired private ProductProcessRouteRepository routeRepo;
    @Autowired private ProductionOrderRepository orderRepo;
    @Autowired private ProductSerialRepository serialRepo;
    @Autowired private ProductionHistoryRepository historyRepo;
    @Autowired private DefectRepository defectRepo;
    @Autowired private DefectLogRepository defectLogRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepo.count() > 0) {
            log.info("Du lieu da ton tai, bo qua khoi tao.");
            return;
        }
        log.info("=== Khoi tao du lieu mau MES ===");

        // 1. ROLES
        Role supRole = roleRepo.save(new Role("SUPERVISOR", "Qu\u1EA3n l\u00FD s\u1EA3n xu\u1EA5t"));
        Role opRole  = roleRepo.save(new Role("OPERATOR",   "C\u00F4ng nh\u00E2n s\u1EA3n xu\u1EA5t"));
        Role qcRole  = roleRepo.save(new Role("QC",         "Nh\u00E2n vi\u00EAn ki\u1EC3m tra ch\u1EA5t l\u01B0\u1EE3ng"));

        // 2. USERS
        User sup1 = createUser("NV001", "Nguy\u1EC5n V\u0103n An",        "supervisor",  "123456", supRole);
        User sup2 = createUser("NV007", "\u0110\u1EB7ng V\u0103n Giang",   "supervisor2", "123456", supRole);
        User op1  = createUser("NV002", "Tr\u1EA7n Th\u1ECB B\u00ECnh",    "operator",    "123456", opRole);
        User op2  = createUser("NV004", "Ph\u1EA1m Th\u1ECB Dung",         "operator2",   "123456", opRole);
        User op3  = createUser("NV005", "Ho\u00E0ng V\u0103n Em",          "operator3",   "123456", opRole);
        User qc1  = createUser("NV003", "L\u00EA V\u0103n C\u01B0\u1EDDng","qc",          "123456", qcRole);
        User qc2  = createUser("NV006", "V\u0169 Th\u1ECB Ph\u01B0\u01A1ng","qc2",        "123456", qcRole);
        log.info("Da tao {} nguoi dung", userRepo.count());

        // 3. PRODUCTS
        Product pcb = createProduct("PCB001", "Bo m\u1EA1ch \u0111i\u1EC1u khi\u1EC3n ch\u00EDnh",  "PCB",
                "Bo m\u1EA1ch ch\u1EE7 \u0111i\u1EC1u khi\u1EC3n h\u1EC7 th\u1ED1ng nh\u00FAng ARM Cortex-M4");
        Product sen = createProduct("SEN001", "C\u1EA3m bi\u1EBFn nhi\u1EC7t \u0111\u1ED9 NTC",     "Sensor",
                "C\u1EA3m bi\u1EBFn \u0111o nhi\u1EC7t \u0111\u1ED9 NTC 10K, d\u1EA3i \u0111o -40 \u0111\u1EBFn 125 \u0111\u1ED9 C");
        Product mod = createProduct("MOD001", "Module ngu\u1ED3n 12V/5A",                            "Power Module",
                "Module chuy\u1EC3n \u0111\u1ED5i ngu\u1ED3n AC220V sang DC12V 5A c\u00F3 b\u1EA3o v\u1EC7 ng\u1EAFn m\u1EA1ch");
        Product led = createProduct("LED001", "Module LED hi\u1EC3n th\u1ECB 7 \u0111o\u1EA1n",      "Display",
                "Module hi\u1EC3n th\u1ECB 7 \u0111o\u1EA1n 4 ch\u1EEF s\u1ED1, giao ti\u1EBFp SPI t\u1ED1c \u0111\u1ED9 cao");
        Product mcu = createProduct("MCU001", "Vi \u0111i\u1EC1u khi\u1EC3n STM32F407",             "MCU",
                "Vi \u0111i\u1EC1u khi\u1EC3n ARM Cortex-M4 168MHz, 1MB Flash, 192KB RAM");
        log.info("Da tao {} san pham", productRepo.count());

        // 4. PROCESS STEPS
        ProcessStep smt      = createStep("SMT",      "SMT - D\u00E1n linh ki\u1EC7n",
                "D\u00E1n linh ki\u1EC7n b\u1EC1 m\u1EB7t SMD b\u1EB1ng m\u00E1y pick-and-place t\u1EF1 \u0111\u1ED9ng");
        ProcessStep assembly = createStep("ASSEMBLY", "Assembly - L\u1EAFp r\u00E1p",
                "L\u1EAFp r\u00E1p linh ki\u1EC7n xuy\u00EAn l\u1ED7, k\u1EBFt n\u1ED1i c\u01A1 kh\u00ED v\u00E0 g\u1EAFn connector");
        ProcessStep solder   = createStep("SOLDER",   "Soldering - H\u00E0n thi\u1EBFc",
                "H\u00E0n ch\u00E2n linh ki\u1EC7n b\u1EB1ng m\u00E1y h\u00E0n s\u00F3ng t\u1EF1 \u0111\u1ED9ng ho\u1EB7c h\u00E0n tay");
        ProcessStep qcTest   = createStep("QCTEST",   "QC Test - Ki\u1EC3m tra ch\u1EA5t l\u01B0\u1EE3ng",
                "Ki\u1EC3m tra \u0111i\u1EC7n, \u0111o th\u00F4ng s\u1ED1 k\u1EF9 thu\u1EADt v\u00E0 ki\u1EC3m tra ch\u1EE9c n\u0103ng s\u1EA3n ph\u1EA9m");
        ProcessStep pack     = createStep("PACKAGING","Packaging - \u0110\u00F3ng g\u00F3i",
                "V\u1EC7 sinh s\u1EA3n ph\u1EA9m, ki\u1EC3m tra ngo\u1EA1i quan l\u1EA7n cu\u1ED1i v\u00E0 \u0111\u00F3ng g\u00F3i th\u00E0nh ph\u1EA9m");
        log.info("Da tao {} cong doan", stepRepo.count());

        // 5. ROUTES
        addRoute(pcb, smt, 1); addRoute(pcb, assembly, 2); addRoute(pcb, solder, 3);
        addRoute(pcb, qcTest, 4); addRoute(pcb, pack, 5);
        addRoute(sen, assembly, 1); addRoute(sen, solder, 2); addRoute(sen, qcTest, 3); addRoute(sen, pack, 4);
        addRoute(mod, smt, 1); addRoute(mod, solder, 2); addRoute(mod, qcTest, 3); addRoute(mod, pack, 4);
        addRoute(led, assembly, 1); addRoute(led, solder, 2); addRoute(led, qcTest, 3); addRoute(led, pack, 4);
        addRoute(mcu, smt, 1); addRoute(mcu, assembly, 2); addRoute(mcu, solder, 3);
        addRoute(mcu, qcTest, 4); addRoute(mcu, pack, 5);

        // 6. DEFECTS
        Defect dHo   = createDefect("DEF001", "H\u1EDF m\u1EA1ch",
                "M\u1EA1ch b\u1ECB h\u1EDF, kh\u00F4ng th\u00F4ng m\u1EA1ch gi\u1EEFa c\u00E1c \u0111i\u1EC3m h\u00E0n");
        Defect dChap = createDefect("DEF002", "Ch\u1EADp m\u1EA1ch",
                "M\u1EA1ch b\u1ECB ch\u1EADp, g\u00E2y ng\u1EAFn m\u1EA1ch ngu\u1ED3n \u0111i\u1EC7n");
        Defect dHan  = createDefect("DEF003", "H\u00E0n thi\u1EBFu thi\u1EBFc",
                "Ch\u00E2n h\u00E0n thi\u1EBFu thi\u1EBFc, ti\u1EBFp x\u00FAc \u0111i\u1EC7n k\u00E9m ch\u1EA5t l\u01B0\u1EE3ng");
        Defect dLech = createDefect("DEF004", "Linh ki\u1EC7n l\u1EC7ch v\u1ECB tr\u00ED",
                "Linh ki\u1EC7n d\u00E1n kh\u00F4ng \u0111\u00FAng v\u1ECB tr\u00ED pad, l\u1EC7ch t\u00E2m qu\u00E1 25%");
        Defect dBui  = createDefect("DEF005", "B\u1EE5i b\u1EA9n tr\u00EAn m\u1EA1ch",
                "Bo m\u1EA1ch c\u00F3 b\u1EE5i, c\u1EB7n b\u1EA9n \u1EA3nh h\u01B0\u1EDFng \u0111\u1EBFn ch\u1EE9c n\u0103ng ho\u1EA1t \u0111\u1ED9ng");
        Defect dNut  = createDefect("DEF006", "N\u1EE9t PCB",
                "Bo m\u1EA1ch b\u1ECB n\u1EE9t, g\u00E3y l\u1EDBp \u0111\u1ED3ng d\u1EABn \u0111i\u1EC7n b\u00EAn trong");
        Defect dOxit = createDefect("DEF007", "Oxi h\u00F3a ch\u00E2n linh ki\u1EC7n",
                "Ch\u00E2n linh ki\u1EC7n b\u1ECB oxi h\u00F3a, kh\u00F3 h\u00E0n v\u00E0 ti\u1EBFp x\u00FAc k\u00E9m");
        Defect dFunc = createDefect("DEF008", "Linh ki\u1EC7n l\u1ED7i ch\u1EE9c n\u0103ng",
                "Linh ki\u1EC7n \u0111\u1EA1t ngo\u1EA1i quan nh\u01B0ng sai gi\u00E1 tr\u1ECB ho\u1EB7c ch\u1EE9c n\u0103ng");
        log.info("Da tao {} loai loi", defectRepo.count());

        // 7. PRODUCTION ORDERS + SERIALS + HISTORY

        // PO001: PCB001 x10, IN_PROGRESS
        ProductionOrder po1 = createOrder("PO001", pcb, 10, sup1, "2026-05-01", "2026-05-31", "IN_PROGRESS");
        List<ProductSerial> po1s = serialRepo.findByProductionOrderId(po1.getId());

        setSerial(po1s.get(0), null, "FINISHED");
        rh(po1s.get(0), smt,      op1, "OK", "D\u00E1n linh ki\u1EC7n SMD \u0111\u1EA1t chu\u1EA9n IPC-A-610D", 6, 8);
        rh(po1s.get(0), assembly, op2, "OK", "L\u1EAFp r\u00E1p connector v\u00E0 jumper ho\u00E0n t\u1EA5t", 6, 6);
        rh(po1s.get(0), solder,   op2, "OK", "H\u00E0n s\u00F3ng, ki\u1EC3m tra k\u1EF9 d\u01B0\u1EDBi k\u00EDnh l\u00FAp 10x", 6, 4);
        rh(po1s.get(0), qcTest,   qc1, "OK", "ICT test 100% pass, \u0111o d\u00F2ng ti\u00EAu th\u1EE5 320mA", 5, 8);
        rh(po1s.get(0), pack,     op3, "OK", "\u0110\u00F3ng g\u00F3i t\u00FAi ESD, d\u00E1n nh\u00E3n barcode v\u00E0 QR code", 5, 6);

        setSerial(po1s.get(1), null, "FINISHED");
        rh(po1s.get(1), smt,      op1, "OK", "D\u00E1n chip IC v\u00E0 t\u1EE5 \u0111\u1EA1t y\u00EAu c\u1EA7u", 5, 8);
        rh(po1s.get(1), assembly, op2, "OK", "G\u1EAFn header pin v\u00E0 relay", 5, 6);
        rh(po1s.get(1), solder,   op3, "OK", "H\u00E0n tay 45 ph\u00FAt, ki\u1EC3m tra t\u1EEBng ch\u00E2n", 5, 4);
        rh(po1s.get(1), qcTest,   qc2, "OK", "Functional test pass, burn-in 30 ph\u00FAt OK", 4, 8);
        rh(po1s.get(1), pack,     op1, "OK", "\u0110\u00F3ng g\u00F3i c\u1EA9n th\u1EADn, ch\u00E8n foam ch\u1ED1ng s\u1EADc", 4, 6);

        setSerial(po1s.get(2), null, "FINISHED");
        rh(po1s.get(2), smt,      op3, "OK", null, 4, 8);
        rh(po1s.get(2), assembly, op1, "OK", "L\u1EAFp r\u00E1p \u0111\u00FAng theo b\u1EA3n v\u1EBD k\u1EF9 thu\u1EADt", 4, 6);
        rh(po1s.get(2), solder,   op2, "OK", null, 4, 4);
        rh(po1s.get(2), qcTest,   qc1, "OK", "T\u1EA5t c\u1EA3 c\u00E1c \u0111i\u1EC3m \u0111o \u0111\u1EA1t ti\u00EAu chu\u1EA9n", 3, 8);
        rh(po1s.get(2), pack,     op3, "OK", null, 3, 6);

        setSerial(po1s.get(3), pack, "IN_PROGRESS");
        rh(po1s.get(3), smt,      op2, "OK", null, 3, 8);
        rh(po1s.get(3), assembly, op2, "OK", null, 3, 6);
        rh(po1s.get(3), solder,   op3, "OK", "H\u00E0n \u0111\u1EB9p, kh\u00F4ng c\u00F3 m\u1ED1i h\u00E0n th\u1EEBa", 2, 8);
        rh(po1s.get(3), qcTest,   qc2, "OK", "Ki\u1EC3m tra \u0111i\u1EC7n \u00E1p v\u00E0 t\u1EA7n s\u1ED1 \u0111\u1EA1t", 2, 6);

        setSerial(po1s.get(4), qcTest, "NG");
        rh(po1s.get(4), smt,      op1, "OK", null, 3, 8);
        rh(po1s.get(4), assembly, op1, "OK", null, 3, 6);
        rh(po1s.get(4), solder,   op2, "OK", null, 2, 8);
        rh(po1s.get(4), qcTest,   qc1, "NG", "D\u00F2ng \u0111o v\u01B0\u1EE3t ng\u01B0\u1EE1ng 480mA, nghi ng\u1EDD ch\u1EADp m\u1EA1ch t\u1EA1i U5", 2, 6);
        dl(po1s.get(4), dChap, qcTest, qc1, "G\u1EEDi l\u1EA1i ki\u1EC3m tra Soldering", "D\u00F2ng \u0111i\u1EC7n v\u01B0\u1EE3t 35% so v\u1EDBi ti\u00EAu chu\u1EA9n");

        setSerial(po1s.get(5), assembly, "SCRAP");
        rh(po1s.get(5), smt,      op3, "OK", null, 2, 8);
        rh(po1s.get(5), assembly, op2, "NG", "IC U3 b\u1ECB v\u1EE1 ch\u00E2n khi l\u1EAFp v\u00E0o socket", 2, 6);
        rh(po1s.get(5), assembly, op2, "SCRAP", "Kh\u00F4ng th\u1EC3 kh\u1EAFc ph\u1EE5c, h\u1EE7y s\u1EA3n ph\u1EA9m", 2, 4);
        dl(po1s.get(5), dLech, assembly, qc2, "H\u1EE7y s\u1EA3n ph\u1EA9m l\u1ED7i", "IC U3 v\u1EE1 2 ch\u00E2n do l\u1EF1c t\u00E1c \u0111\u1ED9ng qu\u00E1 m\u1EA1nh khi l\u1EAFp");

        setSerial(po1s.get(6), smt, "HOLD");
        rh(po1s.get(6), smt, op2, "HOLD", "Ph\u00E1t hi\u1EC7n v\u1EBFt n\u1EE9t nh\u1ECF tr\u00EAn l\u1EDBp \u0111\u1ED3ng PCB", 1, 6);
        dl(po1s.get(6), dNut, smt, qc1, "Ch\u1EDD ph\u00E2n t\u00EDch k\u1EF9 thu\u1EADt", "V\u1EBFt n\u1EE9t d\u00E0i 2mm t\u1EA1i v\u00F9ng U1");

        setSerial(po1s.get(7), solder, "NG");
        rh(po1s.get(7), smt,      op1, "OK", null, 1, 8);
        rh(po1s.get(7), assembly, op3, "OK", null, 1, 6);
        rh(po1s.get(7), solder,   op2, "NG", "Ph\u00E1t hi\u1EC7n c\u1EA7u thi\u1EBFc gi\u1EEFa ch\u00E2n 3-4 c\u1EE7a J5", 0, 6);
        dl(po1s.get(7), dChap, solder, qc2, "H\u00FAt thi\u1EBFc v\u00E0 h\u00E0n l\u1EA1i", "C\u1EA7u thi\u1EBFc d\u1EABn \u0111\u1EBFn ng\u1EAFn m\u1EA1ch");

        po1.setCompletedQuantity(3);
        orderRepo.save(po1);

        // PO002: SEN001 x8, IN_PROGRESS
        ProductionOrder po2 = createOrder("PO002", sen, 8, sup1, "2026-05-05", "2026-05-25", "IN_PROGRESS");
        List<ProductSerial> po2s = serialRepo.findByProductionOrderId(po2.getId());

        setSerial(po2s.get(0), null, "FINISHED");
        rh(po2s.get(0), assembly, op3, "OK", "L\u1EAFp r\u00E1p c\u1EA3m bi\u1EBFn NTC v\u00E0o \u0111\u1EBF nh\u1EF1a c\u1EA9n th\u1EADn", 4, 6);
        rh(po2s.get(0), solder,   op2, "OK", "H\u00E0n tay, ki\u1EC3m tra d\u01B0\u1EDBi k\u00EDnh l\u00FAp 20x", 4, 4);
        rh(po2s.get(0), qcTest,   qc2, "OK", "\u0110o \u0111i\u1EC7n tr\u1EDF 10k\u03A9 \u00B1 0.5% t\u1EA1i 25 \u0111\u1ED9 C, \u0111\u1EA1t ti\u00EAu chu\u1EA9n", 3, 6);
        rh(po2s.get(0), pack,     op3, "OK", "\u0110\u00F3ng g\u00F3i v\u00E0o t\u00FAi PE ch\u1ED1ng \u1EA9m c\u00F3 h\u00FAt ch\u00E2n kh\u00F4ng", 3, 4);

        setSerial(po2s.get(1), null, "FINISHED");
        rh(po2s.get(1), assembly, op1, "OK", null, 3, 8);
        rh(po2s.get(1), solder,   op2, "OK", "H\u00E0n s\u1EA1ch, kh\u00F4ng c\u00F3 m\u1ED1i th\u1EEBa", 3, 6);
        rh(po2s.get(1), qcTest,   qc1, "OK", "Ki\u1EC3m tra \u0111\u1EB7c tuy\u1EBFn nhi\u1EC7t \u0111\u1ED9 \u0111\u1EA1t chu\u1EA9n", 2, 8);
        rh(po2s.get(1), pack,     op3, "OK", null, 2, 6);

        setSerial(po2s.get(2), null, "FINISHED");
        rh(po2s.get(2), assembly, op3, "OK", null, 2, 8);
        rh(po2s.get(2), solder,   op3, "OK", null, 2, 6);
        rh(po2s.get(2), qcTest,   qc2, "OK", "Test d\u1EA3i nhi\u1EC7t \u0111\u1ED9 -20 \u0111\u1EBFn 80 \u0111\u1ED9 C, gi\u00E1 tr\u1ECB \u1ED5n \u0111\u1ECBnh", 1, 8);
        rh(po2s.get(2), pack,     op1, "OK", null, 1, 6);

        setSerial(po2s.get(3), qcTest, "IN_PROGRESS");
        rh(po2s.get(3), assembly, op3, "OK", null, 1, 6);
        rh(po2s.get(3), solder,   op2, "OK", "H\u00E0n s\u1EA1ch, kh\u00F4ng c\u00F3 thi\u1EBFc th\u1EEBa", 1, 4);

        setSerial(po2s.get(4), solder, "IN_PROGRESS");
        rh(po2s.get(4), assembly, op1, "OK", null, 0, 6);

        setSerial(po2s.get(5), solder, "NG");
        rh(po2s.get(5), assembly, op3, "OK", null, 1, 8);
        rh(po2s.get(5), solder,   op2, "NG", "M\u1ED1i h\u00E0n kh\u00F4ng d\u00EDnh, thi\u1EBFc kh\u00F4ng ch\u1EA3y \u0111\u1EC1u", 1, 6);
        dl(po2s.get(5), dHan, solder, qc2, "V\u1EC7 sinh m\u1ECF h\u00E0n v\u00E0 h\u00E0n l\u1EA1i", "Thi\u1EBFc kh\u00F4ng th\u1EA5m do flux b\u1ECB nhi\u1EC5m b\u1EA9n");

        setSerial(po2s.get(6), solder, "IN_PROGRESS");
        rh(po2s.get(6), assembly, op1, "OK", null, 2, 8);
        rh(po2s.get(6), solder,   op2, "NG",     "Thi\u1EBFc th\u1EEBa t\u1EA1o c\u1EA7u ch\u1EADp gi\u1EEFa ch\u00E2n 2-3", 2, 6);
        rh(po2s.get(6), solder,   op3, "REWORK", "H\u00FAt thi\u1EBFc th\u1EEBa b\u1EB1ng b\u1EA5c h\u00FAt, h\u00E0n l\u1EA1i c\u1EA9n th\u1EADn", 1, 4);
        dl(po2s.get(6), dChap, solder, qc1, "H\u00FAt thi\u1EBFc v\u00E0 h\u00E0n l\u1EA1i", "C\u1EA7u thi\u1EBFc ng\u1EAFn m\u1EA1ch pin 2-3 c\u1EE7a U2");

        po2.setCompletedQuantity(3);
        orderRepo.save(po2);

        // PO003: MOD001 x6, COMPLETED
        ProductionOrder po3 = createOrder("PO003", mod, 6, sup2, "2026-04-20", "2026-05-10", "COMPLETED");
        List<ProductSerial> po3s = serialRepo.findByProductionOrderId(po3.getId());

        for (int i = 0; i < 4; i++) {
            setSerial(po3s.get(i), null, "FINISHED");
            rh(po3s.get(i), smt,    op2, "OK", "D\u00E1n linh ki\u1EC7n \u0111\u1EA1t chu\u1EA9n", 14 - i * 2, 8);
            rh(po3s.get(i), solder, op3, "OK", null, 14 - i * 2, 6);
            rh(po3s.get(i), qcTest, qc2, "OK", "Ki\u1EC3m tra \u0111i\u1EC7n \u00E1p \u0111\u1EA7u ra 12.0V \u00B1 0.3%", 13 - i * 2, 8);
            rh(po3s.get(i), pack,   op1, "OK", null, 13 - i * 2, 6);
        }

        setSerial(po3s.get(4), null, "SCRAP");
        rh(po3s.get(4), smt,    op2, "OK",    null, 18, 8);
        rh(po3s.get(4), solder, op3, "NG",    "Bi\u1EBFn \u00E1p ph\u00E1t nhi\u1EC7t b\u1EA5t th\u01B0\u1EDDng khi test t\u1EA3i 80%", 18, 6);
        rh(po3s.get(4), solder, op3, "SCRAP", "Bi\u1EBFn \u00E1p ch\u00E1y kh\u00F4ng th\u1EC3 thay th\u1EBF", 18, 4);
        dl(po3s.get(4), dFunc, solder, qc2, "H\u1EE7y s\u1EA3n ph\u1EA9m b\u1ECB ch\u00E1y", "Bi\u1EBFn \u00E1p ch\u00E1y khi t\u1EA3i 4A");

        setSerial(po3s.get(5), null, "FINISHED");
        rh(po3s.get(5), smt,    op2, "OK",     null, 16, 8);
        rh(po3s.get(5), solder, op3, "NG",     "M\u1ED1i h\u00E0n bi\u1EBFn \u00E1p T1 kh\u00F4ng \u0111\u1EC1u", 16, 6);
        rh(po3s.get(5), solder, op3, "REWORK", "H\u00E0n l\u1EA1i bi\u1EBFn \u00E1p T1, ki\u1EC3m tra k\u1EF9 t\u1EEBng ch\u00E2n", 15, 8);
        rh(po3s.get(5), qcTest, qc1, "OK",     "Sau rework \u0111\u1EA1t ti\u00EAu chu\u1EA9n, \u0111i\u1EC7n \u00E1p 12.02V", 15, 6);
        rh(po3s.get(5), pack,   op1, "OK",     null, 14, 8);

        po3.setCompletedQuantity(5);
        orderRepo.save(po3);

        // PO004: LED001 x12, IN_PROGRESS
        ProductionOrder po4 = createOrder("PO004", led, 12, sup1, "2026-05-08", "2026-06-05", "IN_PROGRESS");
        List<ProductSerial> po4s = serialRepo.findByProductionOrderId(po4.getId());

        for (int i = 0; i < 4; i++) {
            setSerial(po4s.get(i), null, "FINISHED");
            rh(po4s.get(i), assembly, op3, "OK", "L\u1EAFp r\u00E1p LED v\u00E0 m\u1EAFt k\u00EDnh \u0111\u1EA7y \u0111\u1EE7", 10 - i * 2, 8);
            rh(po4s.get(i), solder,   op2, "OK", null, 10 - i * 2, 6);
            rh(po4s.get(i), qcTest,   qc2, "OK", "Ki\u1EC3m tra \u0111\u1ED9 s\u00E1ng \u0111\u1EA1t ti\u00EAu chu\u1EA9n", 9 - i * 2, 8);
            rh(po4s.get(i), pack,     op1, "OK", null, 9 - i * 2, 6);
        }

        setSerial(po4s.get(4), qcTest, "IN_PROGRESS");
        rh(po4s.get(4), assembly, op3, "OK", null, 2, 8);
        rh(po4s.get(4), solder,   op2, "OK", "H\u00E0n s\u1EA1ch, ti\u1EBFp x\u00FAc t\u1ED1t", 2, 6);

        setSerial(po4s.get(5), solder, "IN_PROGRESS");
        rh(po4s.get(5), assembly, op3, "OK", null, 1, 8);

        setSerial(po4s.get(6), assembly, "IN_PROGRESS");

        setSerial(po4s.get(7), qcTest, "NG");
        rh(po4s.get(7), assembly, op3, "OK", null, 5, 8);
        rh(po4s.get(7), solder,   op2, "OK", null, 5, 6);
        rh(po4s.get(7), qcTest,   qc2, "NG", "LED m\u1EDD kh\u00F4ng s\u00E1ng \u0111\u1EE7 \u0111\u1ED9 s\u00E1ng quy \u0111\u1ECBnh", 4, 8);
        dl(po4s.get(7), dOxit, qcTest, qc2, "Thay LED oxi h\u00F3a", "3 LED oxi h\u00F3a ch\u00E2n, \u0111i\u1EC7n tr\u1EDF ti\u1EBFp x\u00FAc t\u0103ng cao");

        setSerial(po4s.get(8), assembly, "SCRAP");
        rh(po4s.get(8), assembly, op3, "SCRAP", "Bo m\u1EA1ch v\u1EE1 khi l\u1EAFp r\u00E1p", 3, 8);
        dl(po4s.get(8), dNut, assembly, qc2, "H\u1EE7y", "PCB n\u1EE9t kh\u00F4ng kh\u1EAFc ph\u1EE5c \u0111\u01B0\u1EE3c");

        po4.setCompletedQuantity(4);
        orderRepo.save(po4);

        // PO005: MCU001 x5, CREATED - all WAITING
        createOrder("PO005", mcu, 5, sup2, "2026-05-15", "2026-06-15", "CREATED");

        // PO006: PCB001 x10, IN_PROGRESS
        ProductionOrder po6 = createOrder("PO006", pcb, 10, sup2, "2026-05-10", "2026-06-10", "IN_PROGRESS");
        List<ProductSerial> po6s = serialRepo.findByProductionOrderId(po6.getId());

        setSerial(po6s.get(0), null, "FINISHED");
        rh(po6s.get(0), smt,      op1, "OK", null, 8, 8);
        rh(po6s.get(0), assembly, op2, "OK", "L\u1EAFp r\u00E1p \u0111\u1EA1t y\u00EAu c\u1EA7u k\u1EF9 thu\u1EADt", 8, 6);
        rh(po6s.get(0), solder,   op2, "OK", null, 7, 8);
        rh(po6s.get(0), qcTest,   qc2, "OK", "Ki\u1EC3m tra \u0111i\u1EC7n to\u00E0n di\u1EC7n \u0111\u1EA1t", 7, 6);
        rh(po6s.get(0), pack,     op3, "OK", null, 6, 8);

        setSerial(po6s.get(1), null, "FINISHED");
        rh(po6s.get(1), smt,      op3, "OK", null, 7, 8);
        rh(po6s.get(1), assembly, op1, "OK", null, 7, 6);
        rh(po6s.get(1), solder,   op2, "OK", null, 6, 8);
        rh(po6s.get(1), qcTest,   qc1, "OK", null, 6, 6);
        rh(po6s.get(1), pack,     op3, "OK", null, 5, 8);

        setSerial(po6s.get(2), null, "FINISHED");
        rh(po6s.get(2), smt,      op1, "OK", null, 6, 8);
        rh(po6s.get(2), assembly, op3, "OK", null, 6, 6);
        rh(po6s.get(2), solder,   op2, "OK", null, 5, 8);
        rh(po6s.get(2), qcTest,   qc2, "OK", null, 5, 6);
        rh(po6s.get(2), pack,     op1, "OK", null, 4, 8);

        setSerial(po6s.get(3), qcTest, "IN_PROGRESS");
        rh(po6s.get(3), smt,      op1, "OK", null, 3, 8);
        rh(po6s.get(3), assembly, op2, "OK", null, 3, 6);
        rh(po6s.get(3), solder,   op2, "OK", null, 2, 8);

        setSerial(po6s.get(4), solder, "IN_PROGRESS");
        rh(po6s.get(4), smt,      op3, "OK", null, 2, 8);
        rh(po6s.get(4), assembly, op1, "OK", null, 1, 8);

        setSerial(po6s.get(5), assembly, "IN_PROGRESS");
        rh(po6s.get(5), smt, op3, "OK", null, 1, 8);

        setSerial(po6s.get(6), qcTest, "NG");
        rh(po6s.get(6), smt,      op1, "OK", null, 5, 8);
        rh(po6s.get(6), assembly, op2, "OK", null, 5, 6);
        rh(po6s.get(6), solder,   op2, "OK", null, 4, 8);
        rh(po6s.get(6), qcTest,   qc2, "NG", "Test \u0111i\u1EC7n th\u1EA5t b\u1EA1i: ch\u1EADp m\u1EA1ch IC U2", 3, 8);
        dl(po6s.get(6), dChap, qcTest, qc2, "H\u00E0n l\u1EA1i", "Ch\u1EADp ch\u00E2n IC U2");

        setSerial(po6s.get(7), solder, "HOLD");
        rh(po6s.get(7), smt,      op3, "OK",   null, 3, 8);
        rh(po6s.get(7), assembly, op1, "OK",   null, 3, 6);
        rh(po6s.get(7), solder,   op2, "HOLD", "T\u1EA1m d\u1EEBng: thi\u1EBFu m\u1ED9t s\u1ED1 linh ki\u1EC7n", 2, 8);

        po6.setCompletedQuantity(3);
        orderRepo.save(po6);

        log.info("=== Khoi tao du lieu mau hoan tat! ===");
        log.info("  Roles:    {}", roleRepo.count());
        log.info("  Users:    {}", userRepo.count());
        log.info("  Products: {}", productRepo.count());
        log.info("  Steps:    {}", stepRepo.count());
        log.info("  Orders:   {}", orderRepo.count());
        log.info("  Serials:  {}", serialRepo.count());
        log.info("  History:  {}", historyRepo.count());
        log.info("  Defects:  {}", defectRepo.count());
        log.info("  DefLogs:  {}", defectLogRepo.count());
    }

    // ===================== HELPERS =====================

    private User createUser(String code, String name, String username, String pw, Role role) {
        User u = new User();
        u.setEmployeeCode(code); u.setFullName(name); u.setUsername(username);
        u.setPassword(passwordEncoder.encode(pw)); u.setRole(role); u.setIsActive(true);
        return userRepo.save(u);
    }

    private Product createProduct(String code, String name, String type, String desc) {
        Product p = new Product();
        p.setProductCode(code); p.setProductName(name); p.setComponentType(type);
        p.setDescription(desc); p.setStatus("ACTIVE");
        return productRepo.save(p);
    }

    private ProcessStep createStep(String code, String name, String desc) {
        ProcessStep s = new ProcessStep();
        s.setStepCode(code); s.setStepName(name); s.setDescription(desc); s.setIsActive(true);
        return stepRepo.save(s);
    }

    private void addRoute(Product product, ProcessStep step, int order) {
        ProductProcessRoute r = new ProductProcessRoute();
        r.setProduct(product); r.setProcessStep(step);
        r.setStepOrder(order); r.setIsMandatory(true);
        routeRepo.save(r);
    }

    private ProductionOrder createOrder(String code, Product product, int qty,
                                        User creator, String start, String end, String status) {
        ProductionOrder o = new ProductionOrder();
        o.setOrderCode(code); o.setProduct(product);
        o.setPlannedQuantity(qty); o.setStartDate(LocalDate.parse(start));
        o.setEndDate(LocalDate.parse(end)); o.setStatus(status);
        o.setCreatedBy(creator);
        o = orderRepo.save(o);
        for (int i = 1; i <= qty; i++) {
            ProductSerial s = new ProductSerial();
            s.setSerialCode(String.format("%s-%s-%04d", code, product.getProductCode(), i));
            s.setProductionOrder(o);
            s.setProduct(product);
            s.setStatus("WAITING");
            serialRepo.save(s);
        }
        return o;
    }

    private Defect createDefect(String code, String name, String desc) {
        Defect d = new Defect();
        d.setDefectCode(code); d.setDefectName(name); d.setDescription(desc); d.setIsActive(true);
        return defectRepo.save(d);
    }

    private void setSerial(ProductSerial s, ProcessStep step, String status) {
        s.setCurrentStep(step);
        s.setStatus(status);
        serialRepo.save(s);
    }

    private void rh(ProductSerial serial, ProcessStep step, User operator,
                    String result, String notes, int daysAgo, int hoursOffset) {
        ProductionHistory h = new ProductionHistory();
        h.setProductSerial(serial);
        h.setProcessStep(step);
        h.setOperator(operator);
        LocalDateTime time = LocalDateTime.now().minusDays(daysAgo).minusHours(hoursOffset);
        h.setStartTime(time);
        h.setEndTime(time.plusMinutes(45));
        h.setResult(result);
        h.setNotes(notes);
        historyRepo.save(h);
    }

    private void dl(ProductSerial serial, Defect defect, ProcessStep step,
                    User reporter, String action, String notes) {
        DefectLog dl = new DefectLog();
        dl.setProductSerial(serial);
        dl.setDefect(defect);
        dl.setProcessStep(step);
        dl.setReportedBy(reporter);
        dl.setActionTaken(action);
        dl.setNotes(notes);
        defectLogRepo.save(dl);
    }
}
