package com.laundry.lms.config;

import com.laundry.lms.model.*;
import com.laundry.lms.repository.*;
import com.laundry.lms.service.CatalogService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner seedDatabase(UserRepository userRepository,
            LaundryOrderRepository orderRepository,
            TaskRepository taskRepository,
            PaymentRepository paymentRepository,
            MessageRepository messageRepository,
            ServiceCatalogRepository serviceCatalogRepository,
            PressingCategoryPriceRepository pressingPriceRepository,
            PasswordEncoder passwordEncoder,
            CatalogService catalogService) {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }

            // ==========================================
            // Seed Service Catalog
            // ==========================================
            List<ServiceCatalog> services = new ArrayList<>();

            ServiceCatalog laundryWash = new ServiceCatalog();
            laundryWash.setName("Laundry (Wash Only)");
            laundryWash.setServiceType(ServiceType.LAUNDRY_WASH_ONLY);
            laundryWash.setUnit(ServiceUnit.KG);
            laundryWash.setBasePrice(250.0);
            laundryWash.setDescription("Standard wash cycle for everyday garments. Billed per kilogram.");
            services.add(laundryWash);

            ServiceCatalog pressing = new ServiceCatalog();
            pressing.setName("Pressing (Iron Only)");
            pressing.setServiceType(ServiceType.PRESSING_IRON_ONLY);
            pressing.setUnit(ServiceUnit.CATEGORY_ITEM);
            pressing.setBasePrice(0.0); // Price varies by category
            pressing.setDescription("Wrinkle-free finish for all garment types. Billed per item by category.");
            services.add(pressing);

            ServiceCatalog washIron = new ServiceCatalog();
            washIron.setName("Wash & Iron");
            washIron.setServiceType(ServiceType.WASH_AND_IRON);
            washIron.setUnit(ServiceUnit.ITEM);
            washIron.setBasePrice(25.0); // Per item pressing + wash per kg
            washIron.setDescription(
                    "Complete care: washed, dried, and expertly pressed. Pressing rate + 25 LKR per item.");
            services.add(washIron);

            ServiceCatalog dryCleaning = new ServiceCatalog();
            dryCleaning.setName("Dry Cleaning");
            dryCleaning.setServiceType(ServiceType.DRY_CLEANING);
            dryCleaning.setUnit(ServiceUnit.KG);
            dryCleaning.setBasePrice(400.0);
            dryCleaning.setDescription("Professional solvent-based cleaning for delicate fabrics. 400 LKR per kg.");
            services.add(dryCleaning);

            ServiceCatalog express = new ServiceCatalog();
            express.setName("Express Service");
            express.setServiceType(ServiceType.EXPRESS_SERVICE);
            express.setUnit(ServiceUnit.ITEM);
            express.setBasePrice(0.0); // Add-on: 25% of subtotal
            express.setDescription("Prioritize your order with 25% faster turnaround. +25% of selected services.");
            services.add(express);

            ServiceCatalog premium = new ServiceCatalog();
            premium.setName("Premium / Delicate Care");
            premium.setServiceType(ServiceType.PREMIUM_DELICATE_CARE);
            premium.setUnit(ServiceUnit.ITEM);
            premium.setBasePrice(400.0);
            premium.setDescription("Individually handled garments with delicate-only detergents. 400 LKR per item.");
            services.add(premium);

            serviceCatalogRepository.saveAll(services);

            // ==========================================
            // Seed Pressing Category Prices
            // ==========================================
            List<PressingCategoryPrice> pressingPrices = List.of(
                    new PressingCategoryPrice(PressingCategory.SHIRT, 50.0),
                    new PressingCategoryPrice(PressingCategory.TROUSER, 60.0),
                    new PressingCategoryPrice(PressingCategory.JACKET, 100.0),
                    new PressingCategoryPrice(PressingCategory.SAREE, 150.0),
                    new PressingCategoryPrice(PressingCategory.SUIT, 200.0),
                    new PressingCategoryPrice(PressingCategory.DRESS, 80.0),
                    new PressingCategoryPrice(PressingCategory.BLOUSE, 50.0),
                    new PressingCategoryPrice(PressingCategory.SKIRT, 60.0),
                    new PressingCategoryPrice(PressingCategory.COAT, 120.0),
                    new PressingCategoryPrice(PressingCategory.CURTAIN, 150.0),
                    new PressingCategoryPrice(PressingCategory.BEDSHEET, 100.0),
                    new PressingCategoryPrice(PressingCategory.OTHER, 50.0));
            pressingPriceRepository.saveAll(pressingPrices);

            // ==========================================
            // Seed Users with ALL 6 Roles
            // ==========================================

            // Admin
            User admin = new User("Admin", "admin@smartfold.lk",
                    passwordEncoder.encode("admin123"), UserRole.ADMIN);
            userRepository.save(admin);

            // Customers
            List<User> customers = List.of(
                    new User("Nimali Jayasinghe", "nimali@smartfold.lk", passwordEncoder.encode("pass123"),
                            UserRole.CUSTOMER),
                    new User("Ruwan Perera", "ruwan@smartfold.lk", passwordEncoder.encode("pass123"),
                            UserRole.CUSTOMER),
                    new User("Kamal Fernando", "kamal@smartfold.lk", passwordEncoder.encode("pass123"),
                            UserRole.CUSTOMER),
                    new User("Test Customer", "customer@test.lk", passwordEncoder.encode("pass123"),
                            UserRole.CUSTOMER));
            userRepository.saveAll(customers);

            // Laundry Staff
            User laundryStaff = new User("Saman Silva", "laundry@smartfold.lk",
                    passwordEncoder.encode("staff123"), UserRole.LAUNDRY_STAFF);
            userRepository.save(laundryStaff);

            // Delivery Staff
            User deliveryStaff = new User("Ishara Kumara", "delivery@smartfold.lk",
                    passwordEncoder.encode("staff123"), UserRole.DELIVERY_STAFF);
            userRepository.save(deliveryStaff);

            // Finance Staff
            User financeStaff = new User("Dilani Perera", "finance@smartfold.lk",
                    passwordEncoder.encode("staff123"), UserRole.FINANCE_STAFF);
            userRepository.save(financeStaff);

            // Customer Service
            User customerService = new User("Pasan Fernando", "support@smartfold.lk",
                    passwordEncoder.encode("staff123"), UserRole.CUSTOMER_SERVICE);
            userRepository.save(customerService);

            // ==========================================
            // Seed Orders
            // ==========================================
            Random random = new Random();
            List<String> serviceNames = catalogService.getServices();
            List<String> units = catalogService.getUnits();

            List<LaundryOrder> orders = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                User customer = customers.get(random.nextInt(customers.size()));
                LaundryOrder order = new LaundryOrder();
                order.setCustomer(customer);
                order.setServiceType(serviceNames.get(random.nextInt(serviceNames.size())));
                order.setQuantity(1.0 + random.nextInt(5));
                order.setUnit(units.get(random.nextInt(units.size())));
                order.setPrice(BigDecimal.valueOf(500 + random.nextInt(3000)));
                order.setPickupDate(LocalDate.now().minusDays(random.nextInt(5)));
                order.setDeliveryDate(LocalDate.now().plusDays(random.nextInt(5) + 1));
                order.setNotes("Auto-generated demo order");
                order.setStatus(OrderStatus.values()[random.nextInt(OrderStatus.values().length)]);
                PaymentMethod paymentMethod = random.nextBoolean() ? PaymentMethod.COD : PaymentMethod.CARD;
                PaymentStatus paymentStatus = PaymentStatus.values()[random.nextInt(PaymentStatus.values().length)];
                order.setPaymentMethod(paymentMethod.name());
                order.setPaymentStatus(paymentStatus.name());
                if (paymentStatus == PaymentStatus.PAID) {
                    order.setPaidAt(Instant.now().minusSeconds(random.nextInt(86_400)));
                }
                orders.add(order);
            }
            orderRepository.saveAll(orders);

            // ==========================================
            // Seed Tasks
            // ==========================================
            List<Task> tasks = new ArrayList<>();
            String[] team = { "Saman", "Ishara", "Dilani", "Pasan" };
            for (int i = 0; i < 12; i++) {
                Task task = new Task();
                task.setTitle("Task #" + (i + 1));
                task.setAssignedTo(team[random.nextInt(team.length)]);
                task.setDueDate(LocalDate.now().plusDays(random.nextInt(7)));
                task.setPrice(BigDecimal.valueOf(200 + random.nextInt(1500)));
                task.setNotes("Demo task generated for showcase");
                task.setStatus(TaskStatus.values()[random.nextInt(TaskStatus.values().length)]);
                tasks.add(task);
            }
            taskRepository.saveAll(tasks);

            // ==========================================
            // Seed Payments
            // ==========================================
            List<Payment> payments = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                LaundryOrder order = orders.get(random.nextInt(orders.size()));
                Payment payment = new Payment();
                payment.setOrderId(order.getId());
                payment.setAmountLkr(order.getPrice());
                PaymentMethod method = PaymentMethod.valueOf(order.getPaymentMethod());
                payment.setMethod(method);
                PaymentStatus status = PaymentStatus.valueOf(order.getPaymentStatus());
                payment.setStatus(status);
                payment.setProvider(method == PaymentMethod.COD ? "CASH" : "DEMO");
                if (status == PaymentStatus.PAID) {
                    order.setPaidAt(order.getPaidAt() != null ? order.getPaidAt() : Instant.now());
                }
                payments.add(payment);
            }
            paymentRepository.saveAll(payments);

            // ==========================================
            // Seed Messages
            // ==========================================
            List<Message> messages = new ArrayList<>();
            for (User customer : customers) {
                for (int i = 0; i < 3; i++) {
                    Message fromCustomer = new Message(customer, customerService,
                            "Hello team, I need help with order #" + (i + 1));
                    messages.add(fromCustomer);
                    Message fromSupport = new Message(customerService, customer,
                            "Hi " + customer.getName().split(" ")[0] + ", I'm happy to assist you with your order.");
                    messages.add(fromSupport);
                }
            }
            messageRepository.saveAll(messages);

            System.out.println("=".repeat(50));
            System.out.println("Database seeding completed!");
            System.out.println("=".repeat(50));
            System.out.println("Users created with roles:");
            System.out.println("  ADMIN:            admin@smartfold.lk / admin123");
            System.out.println("  CUSTOMER:         customer@test.lk / pass123");
            System.out.println("  LAUNDRY_STAFF:    laundry@smartfold.lk / staff123");
            System.out.println("  DELIVERY_STAFF:   delivery@smartfold.lk / staff123");
            System.out.println("  FINANCE_STAFF:    finance@smartfold.lk / staff123");
            System.out.println("  CUSTOMER_SERVICE: support@smartfold.lk / staff123");
            System.out.println("=".repeat(50));
        };
    }
}
