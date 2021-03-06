package kr.purred.startboot2.model.reservation.imp;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import kr.purred.startboot2.model.reservation.ReservationNotAvailableException;
import kr.purred.startboot2.model.reservation.ReservationService;
import kr.purred.startboot2.model.reservation.domain.PeriodicReservation;
import kr.purred.startboot2.model.reservation.domain.Player;
import kr.purred.startboot2.model.reservation.domain.Reservation;
import kr.purred.startboot2.model.reservation.domain.SportType;

@Service
public class ReservationServiceImp implements ReservationService
{
	public static final SportType TENNIS = new SportType (1, "Tennis");
	public static final SportType SOCCER = new SportType (2, "Soccer");

	private final List<Reservation> reservations = new ArrayList<> ();

	public ReservationServiceImp ()
	{
		reservations.add (new Reservation ("Tennis #1", java.sql.Date.valueOf (LocalDate.of (2008, 1, 14)), 16, new Player ("Roger", "N/A"), TENNIS));
		reservations.add (new Reservation ("Tennis #2", java.sql.Date.valueOf (LocalDate.of (2008, 1, 14)), 20, new Player ("Roger", "N/A"), TENNIS));
	}

	@Override
	public List<Reservation> query (String courtName)
	{
		return reservations.stream ().filter ((r) -> r.getCourtName ().equals (courtName)).collect(Collectors.toList ());
	}

	@Override
	public List<Reservation> all ()
	{
		return new ArrayList<> (reservations);
	}

	@Override
	public void make (Reservation reservation) throws ReservationNotAvailableException
	{
		long cnt = reservations.stream ()
				.filter ((m) -> m.getCourtName ().equals (reservation.getCourtName ()))
				.filter ((m) -> m.getDate ().equals (reservation.getDate ()))
				.filter ((m) -> m.getHour () == reservation.getHour ())
				.count ();

		if (cnt > 0)
			throw new ReservationNotAvailableException (reservation.getCourtName (), reservation.getDate (), reservation.getHour ());

		reservations.add (reservation);
	}

	@Override
	public List<SportType> getAllSportTypes ()
	{
		return Arrays.asList (TENNIS, SOCCER);
	}

	@Override
	public SportType getSportType (int sportTypeId)
	{
		switch (sportTypeId)
		{
			case 1:
				return TENNIS;
			case 2:
				return SOCCER;
			default:
				return null;
		}
	}

	@Override
	public void makePeriodic (PeriodicReservation periodicReservation) throws ReservationNotAvailableException
	{
		LocalDate fromDate = new java.sql.Date (periodicReservation.getFromDate ().getTime ()).toLocalDate ();
		LocalDate toDate = new java.sql.Date (periodicReservation.getToDate ().getTime ()).toLocalDate ();

		while (fromDate.isBefore (toDate))
		{
			Reservation reservation = new Reservation ();
			reservation.setCourtName (periodicReservation.getCountName ());
			reservation.setDate (java.sql.Date.valueOf (fromDate));
			reservation.setHour (periodicReservation.getHour ());
			reservation.setPlayer (periodicReservation.getPlayer ());

			make (reservation);

			fromDate = fromDate.plusDays (periodicReservation.getPeriod ());
		}
	}

	@Override
	public List<Reservation> findByDate (LocalDate date)
	{
		return reservations.stream ().filter ((r) -> Objects.equals (new java.sql.Date (r.getDate ().getTime ()).toLocalDate (), date)).collect(Collectors.toList());
	}

	@Async
	@Override
	public void testFunc ()
	{
		System.out.println (Thread.currentThread ().toString ());
		System.out.println ("비동기 테스트 입니다.");
	}
}
