@Service
public class AdminService {
    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;

    public AdminService(UserRepository userRepository, BankAccountRepository bankAccountRepository) {
        this.userRepository = userRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    public Page<AdminUserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
            .map(user -> {
                long count = bankAccountRepository.countByOwner(user);
                return AdminUserResponse.from(user, count);
            });
    }
}