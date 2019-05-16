package edu.iis.mto.testreactor.exc3;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

public class AtmMachineTest {

    private CardProviderService cardService;
    private BankService bankService;
    private MoneyDepot moneyDepot;
    private Money.Builder moneyBuilder;
    private Card.Builder cardBuilder;

    @Before
    public void setUp() {
        cardService = mock(CardProviderService.class);
        bankService = mock(BankService.class);
        moneyDepot = mock(MoneyDepot.class);

        moneyBuilder = Money.builder().withAmount(10).withCurrency(Currency.PL);
        cardBuilder = Card.builder().withCardNumber("4840932426207833").withPinNumber(1234);
    }

    @Test
    public void itCompiles() {
        assertThat(true, equalTo(true));
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
}
