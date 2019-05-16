package edu.iis.mto.testreactor.exc3;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import org.hamcrest.FeatureMatcher;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

public class AtmMachineTest {

    private CardProviderService cardService;
    private BankService bankService;
    private MoneyDepot moneyDepot;
    private AuthenticationToken authenticationToken;
    private Money.Builder moneyBuilder;
    private Card.Builder cardBuilder;

    @Before
    public void setUp() {
        cardService = mock(CardProviderService.class);
        bankService = mock(BankService.class);
        moneyDepot = mock(MoneyDepot.class);

        authenticationToken = AuthenticationToken.builder().withUserId("").withAuthorizationCode(1).build();

        when(cardService.authorize(any(Card.class))).thenReturn(Optional.of(authenticationToken));
        when(bankService.charge(any(AuthenticationToken.class), any(Money.class))).thenReturn(true);
        when(moneyDepot.releaseBanknotes(anyListOf(Banknote.class))).thenReturn(true);

        moneyBuilder = Money.builder().withAmount(10).withCurrency(Currency.PL);
        cardBuilder = Card.builder().withCardNumber("4840932426207833").withPinNumber(1234);
    }

    @Test
    public void itCompiles() {
        assertThat(true, equalTo(true));
    }

    @Test
    public void withdraw_releasesRightAmount() {
        AtmMachine atmMachine = new AtmMachine(cardService, bankService, moneyDepot);

        Money money = moneyBuilder.build();
        Card card = cardBuilder.build();

        Payment payment = atmMachine.withdraw(money, card);

        assertThat(payment, totals(money.getAmount()));
    }

    @Test
    public void withdraw_releasesRightCurrency() {
        AtmMachine atmMachine = new AtmMachine(cardService, bankService, moneyDepot);

        Money money = moneyBuilder.build();
        Card card = cardBuilder.build();

        List<Banknote> banknotes = atmMachine.withdraw(money, card).getValue();

        assertThat(banknotes, everyItem(currency(money.getCurrency())));
    }

    @Test(expected = WrongMoneyAmountException.class)
    public void withdraw_zero_throws() {
        AtmMachine atmMachine = new AtmMachine(cardService, bankService, moneyDepot);

        Money money = moneyBuilder.withAmount(0).build();
        Card card = cardBuilder.build();

        atmMachine.withdraw(money, card);
    }

    @Test(expected = WrongMoneyAmountException.class)
    public void withdraw_negative_throws() {
        AtmMachine atmMachine = new AtmMachine(cardService, bankService, moneyDepot);

        Money money = moneyBuilder.withAmount(-1).build();
        Card card = cardBuilder.build();

        atmMachine.withdraw(money, card);
    }

    @Test(expected = WrongMoneyAmountException.class)
    public void withdraw_notDivisibleByNotes_throws() {
        AtmMachine atmMachine = new AtmMachine(cardService, bankService, moneyDepot);

        Money money = moneyBuilder.withAmount(3).build();
        Card card = cardBuilder.build();

        atmMachine.withdraw(money, card);
    }

    @Test
    public void withdraw_triesToAuthorizeCard() {
        AtmMachine atmMachine = new AtmMachine(cardService, bankService, moneyDepot);

        Money money = moneyBuilder.build();
        Card card = cardBuilder.build();

        atmMachine.withdraw(money, card);

        verify(cardService, times(1)).authorize(card);
    }

    @Test(expected = CardAuthorizationException.class)
    public void withdraw_unauthorized_throws() {
        Money money = moneyBuilder.build();
        Card card = cardBuilder.build();

        when(cardService.authorize(card)).thenReturn(Optional.empty());

        AtmMachine atmMachine = new AtmMachine(cardService, bankService, moneyDepot);

        atmMachine.withdraw(money, card);
    }

    @Test
    public void withdraw_chargesAccount() {
        AtmMachine atmMachine = new AtmMachine(cardService, bankService, moneyDepot);

        Money money = moneyBuilder.build();
        Card card = cardBuilder.build();

        atmMachine.withdraw(money, card);

        verify(bankService, times(1)).charge(authenticationToken, money);
    }

    @Test(expected = InsufficientFundsException.class)
    public void withdraw_emptyAccount_throws() {
        Money money = moneyBuilder.build();
        Card card = cardBuilder.build();

        when(bankService.charge(any(AuthenticationToken.class), any(Money.class))).thenReturn(false);

        AtmMachine atmMachine = new AtmMachine(cardService, bankService, moneyDepot);

        atmMachine.withdraw(money, card);
    }

    @Test
    public void withdraw_releasesBanknotes() {
        AtmMachine atmMachine = new AtmMachine(cardService, bankService, moneyDepot);

        Money money = moneyBuilder.build();
        Card card = cardBuilder.build();

        atmMachine.withdraw(money, card);

        verify(moneyDepot, times(1)).releaseBanknotes(anyListOf(Banknote.class));
    }

    @Test(expected = MoneyDepotException.class)
    public void withdraw_noBanknotes_throws() {
        Money money = moneyBuilder.build();
        Card card = cardBuilder.build();

        when(moneyDepot.releaseBanknotes(anyListOf(Banknote.class))).thenReturn(false);

        AtmMachine atmMachine = new AtmMachine(cardService, bankService, moneyDepot);

        atmMachine.withdraw(money, card);
    }

    @Test
    public void withdraw_startsTransaction() {
        AtmMachine atmMachine = new AtmMachine(cardService, bankService, moneyDepot);

        Money money = moneyBuilder.build();
        Card card = cardBuilder.build();

        atmMachine.withdraw(money, card);

        verify(bankService, times(1)).startTransaction(authenticationToken);
    }

    private FeatureMatcher<Payment, Integer> totals(Integer amount) {
        return new FeatureMatcher<Payment, Integer>(equalTo(amount), "totals", "totals") {
            @Override
            protected Integer featureValueOf(Payment actual) {
                List<Banknote> banknotes = actual.getValue();
                return banknotes.stream().mapToInt(Banknote::getValue).sum();
            }
        };
    }

    private FeatureMatcher<Banknote, Currency> currency(Currency currency) {
        return new FeatureMatcher<Banknote, Currency>(equalTo(currency), "currency", "currency") {
            @Override
            protected Currency featureValueOf(Banknote actual) {
                return actual.getCurrency();
            }
        };
    }

}
